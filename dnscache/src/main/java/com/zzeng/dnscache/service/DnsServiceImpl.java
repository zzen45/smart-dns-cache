package com.zzeng.dnscache.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzeng.dnscache.config.DnsProperties;
import com.zzeng.dnscache.dto.DnsRecordMapper;
import com.zzeng.dnscache.dto.DnsRecordResponse;
import com.zzeng.dnscache.model.DnsRecord;
import com.zzeng.dnscache.repository.DnsCacheRepository;
import com.zzeng.dnscache.util.DnsFallbackResolver;
import com.zzeng.dnscache.util.JsonUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.UnknownHostException;
import java.util.List;

@Service
public class DnsServiceImpl implements DnsService {

    private final DnsCacheRepository dnsCacheRepository;
    private final ObjectMapper objectMapper;
    private final long defaultTtl;
    private static final Logger logger = LoggerFactory.getLogger(DnsServiceImpl.class);
    private final List<String> fallbackServers;

    @Autowired
    public DnsServiceImpl(DnsCacheRepository dnsCacheRepository,
                          ObjectMapper objectMapper,
                          DnsProperties dnsProperties) {
        this.dnsCacheRepository = dnsCacheRepository;
        this.objectMapper = objectMapper;
        this.defaultTtl = dnsProperties.getTtl();

        this.fallbackServers = dnsProperties.getFallbackServers();
    }

    @PostConstruct
    public void init() {
        logger.info("DnsServiceImpl initialized with default TTL: {} seconds", defaultTtl);
    }


    // --- Resolution ---
    @Override
    public Mono<DnsRecordResponse> resolveDomain(String domain) {
        return resolveDomain(domain, defaultTtl);
    }

    @Override
    public Mono<DnsRecordResponse> resolveDomain(String domain, Long optionalTtl) {
        return (optionalTtl == null)
                ? resolveDomain(domain)
                : resolveDomain(domain, optionalTtl);
    }

    @Override
    public Mono<DnsRecordResponse> resolveDomain(String domain, long ttlSeconds) {
        return dnsCacheRepository.get(domain)
                .flatMap(json -> JsonUtil.safeDeserialize(json, objectMapper))
                .switchIfEmpty(Mono.defer(() -> resolveAndCache(domain, ttlSeconds)))
                .map(DnsRecordMapper::toResponse);
    }

    private Mono<DnsRecord> resolveAndCache(String domain, long ttlSeconds) {
        return resolveWithFallback(domain)
                .flatMap(ip -> {
                    DnsRecord record = new DnsRecord(domain, ip, ttlSeconds, false);
                    return JsonUtil.safeSerialize(record, objectMapper)
                            .flatMap(json -> dnsCacheRepository.set(domain, json, ttlSeconds)
                                    .thenReturn(record));
                })
                .onErrorResume(err -> {
                    logger.error("DNS resolution failed for domain: {}", domain, err);
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to resolve domain: " + domain));
                });
    }

    private Mono<String> resolveWithFallback(String domain) {
        if (fallbackServers == null || fallbackServers.isEmpty()) {
            return Mono.error(new UnknownHostException("No fallback servers configured"));
        }
        Mono<String> chain = Mono.error(new UnknownHostException("No DNS servers tried yet"));
        for (String server : fallbackServers) {
            final Mono<String> attempt = Mono.fromCallable(() -> DnsFallbackResolver.resolve(domain, server))
                    .subscribeOn(Schedulers.boundedElastic());

            chain = chain.onErrorResume(err -> attempt);
        }

        return chain;
    }


    // --- Create ---
    @Override
    public Mono<DnsRecordResponse> createManualEntry(DnsRecord record) {
        record.setManual(true);
        return JsonUtil.safeSerialize(record, objectMapper)
                .flatMap(json -> dnsCacheRepository.set(record.getDomain(), json, record.getTtl())
                        .thenReturn(record))
                .map(DnsRecordMapper::toResponse);
    }


    // --- Read ---
    @Override
    public Mono<DnsRecordResponse> getCachedRecord(String domain) {
        return dnsCacheRepository.get(domain)
                .flatMap(json -> JsonUtil.safeDeserialize(json, objectMapper))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found")))
                .map(DnsRecordMapper::toResponse);
    }

    @Override
    public Flux<DnsRecordResponse> getAllCachedRecords() {
        return dnsCacheRepository.scanKeys()
                .flatMap(key -> dnsCacheRepository.get(key)
                        .flatMap(json -> JsonUtil.safeDeserialize(json, objectMapper)))
                .map(DnsRecordMapper::toResponse);
    }

    @Override
    public Mono<Boolean> exists(String domain) {
        return dnsCacheRepository.get(domain)
                .map(val -> true)
                .defaultIfEmpty(false);
    }

    @Override
    public Flux<DnsRecordResponse> getBatch(List<String> domains) {
        return Flux.fromIterable(domains)
                .flatMap(domain -> dnsCacheRepository.get(domain)
                        .flatMap(json -> JsonUtil.safeDeserialize(json, objectMapper)))
                .map(DnsRecordMapper::toResponse);
    }


    // --- Update ---
    @Override
    public Mono<Boolean> updateTTL(String domain, long newTTL) {
        return dnsCacheRepository.get(domain)
                .flatMap(json -> JsonUtil.safeDeserialize(json, objectMapper))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found for TTL update")))
                .flatMap(record -> {
                    record.setTtl(newTTL);
                    return JsonUtil.safeSerialize(record, objectMapper)
                            .flatMap(serialized -> dnsCacheRepository.set(domain, serialized, newTTL))
                            .thenReturn(true);
                });
    }


    // --- Delete ---
    @Override
    public Mono<Boolean> deleteCachedRecord(String domain) {
        return dnsCacheRepository.delete(domain)
                .flatMap(deleted -> deleted
                        ? Mono.just(true)
                        : Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No such domain to delete")));
    }

    @Override
    public Mono<String> clearCache() {
        return dnsCacheRepository.scanKeys()
                .flatMap(dnsCacheRepository::delete)
                .then(Mono.just("Cache cleared"));
    }

    @Override
    public Mono<String> deleteAllManualEntries() {
        return dnsCacheRepository.scanKeys()
                .flatMap(key -> dnsCacheRepository.get(key)
                        .flatMap(json -> JsonUtil.safeDeserialize(json, objectMapper))
                        .flatMap(record -> {
                            if (record.isManual()) {
                                return dnsCacheRepository.delete(key)
                                        .filter(Boolean::booleanValue)
                                        .map(deleted -> 1L);
                            } else {
                                return Mono.just(0L);
                            }
                        })
                )
                .reduce(0L, Long::sum)
                .map(count -> "Deleted " + count + " manual entries.");
    }

    @Override
    public Mono<String> deleteBatch(List<String> domains) {
        return Flux.fromIterable(domains)
                .flatMap(dnsCacheRepository::delete)
                .filter(Boolean::booleanValue)
                .count()
                .map(deletedCount -> "Deleted " + deletedCount + " entries.");
    }
}
