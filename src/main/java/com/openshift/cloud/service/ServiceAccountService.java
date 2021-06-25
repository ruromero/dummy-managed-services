package com.openshift.cloud.service;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.cloud.api.kas.models.ServiceAccount;
import com.openshift.cloud.api.kas.models.ServiceAccountList;
import com.openshift.cloud.api.kas.models.ServiceAccountListItem;
import com.openshift.cloud.api.kas.models.ServiceAccountRequest;

@ApplicationScoped
public class ServiceAccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAccountService.class);

    private final Random rnd = new Random();

    private Map<String, ServiceAccount> accounts = new HashMap<>();
    private Map<String, ServiceAccount> staticAccounts = new HashMap<>();

    @ConfigProperty(name = "dms.serviceaccount.static")
    Optional<String> staticConfig;

    @Inject
    ObjectMapper mapper;

    @Inject
    JsonWebToken token;

    @PostConstruct
    public void init() {
        if (staticConfig.isEmpty()) {
            return;
        }
        var f = new File(staticConfig.get());
        if (f.exists()) {
            try {
                List<ServiceAccount> loadedAcc = mapper.readerForListOf(ServiceAccount.class).readValue(f);
                loadedAcc.forEach(sa -> staticAccounts.put(sa.getName(), sa));
            } catch (IOException e) {
                LOGGER.error("Unable to read static accounts");
            }
        }
    }

    public ServiceAccount get(String id) {
        if (accounts.containsKey(id)) {
            LOGGER.info("Returning existing ServiceAccount {}", accounts.get(id));
            return accounts.get(id);
        }
        LOGGER.info("Not found ServiceAccount {}", id);
        return null;
    }

    public ServiceAccountList list() {
        LOGGER.info("List ServiceAccounts");
        var list = new ServiceAccountList();
        accounts.values().forEach(sa -> list.addItemsItem(toListItem(sa)));
        return list;
    }

    public ServiceAccount reset(String id) {
        if (accounts.containsKey(id)) {
            LOGGER.info("Reset credentials for existing ServiceAccount {}", accounts.get(id));
            return setCredentials(accounts.get(id));
        }
        LOGGER.info("Not found ServiceAccount {}", id);
        return null;
    }

    public ServiceAccount create(ServiceAccountRequest request) {
        LOGGER.info("Creating new ServiceAccount {}", request);
        var owner = (String) token.claim("preferred_username").orElse("unknown");
        var serviceAccount = new ServiceAccount();
        serviceAccount.id(UUID.randomUUID().toString());
        serviceAccount.name(request.getName());
        serviceAccount.description(request.getDescription());
        serviceAccount.createdAt(ZonedDateTime.now().toOffsetDateTime());
        serviceAccount.setOwner(owner);
        accounts.put(serviceAccount.getId(), serviceAccount);
        return setCredentials(serviceAccount);
    }

    public void delete(String id) {
        accounts.remove(id);
    }

    private ServiceAccountListItem toListItem(ServiceAccount sa) {
        var item = new ServiceAccountListItem();
        item.setId(sa.getId());
        item.setName(sa.getName());
        item.setDescription(sa.getDescription());
        item.setCreatedAt(sa.getCreatedAt());
        item.setOwner(sa.getOwner());
        item.setHref(sa.getHref());
        item.setKind(sa.getKind());
        item.setClientId(sa.getClientId());
        return item;
    }

    private ServiceAccount setCredentials(ServiceAccount original) {
        var acc = new ServiceAccount();
        acc.setOwner(original.getOwner());
        acc.setName(original.getName());
        acc.setId(original.getId());
        acc.setCreatedAt(original.getCreatedAt());
        acc.setDescription(original.getDescription());
        if (!staticAccounts.containsKey(original.getName())) {
            acc.setClientId("svc-acc" + rnd.nextInt());
            acc.setClientSecret(UUID.randomUUID().toString());
        } else {
            var staticSa = staticAccounts.get(original.getName());
            acc.setClientId(staticSa.getClientId());
            acc.setClientSecret(staticSa.getClientSecret());
        }
        return acc;
    }
}
