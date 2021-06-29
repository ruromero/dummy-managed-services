package com.openshift.cloud.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
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
    private static final String SERVICE_ACCOUNT_KIND = "ServiceAccount";

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
                LOGGER.info("Loading static service accounts from: {}", f.getAbsolutePath());
                List<ServiceAccount> loadedAcc = mapper.readerForListOf(ServiceAccount.class).readValue(f);
                loadedAcc.forEach(sa -> staticAccounts.put(sa.getName(), sa));
            } catch (IOException e) {
                LOGGER.error("Unable to read static accounts", e);
            }
        }
    }

    public ServiceAccount get(String id) {
        if (accounts.containsKey(id)) {
            LOGGER.info("Returning existing ServiceAccount {}", accounts.get(id));
            return withoutCredentials(accounts.get(id));
        }
        LOGGER.info("Not found ServiceAccount {}", id);
        return null;
    }

    public ServiceAccountList list() {
        LOGGER.info("List ServiceAccounts");
        var list = new ServiceAccountList();
        list.setKind(ServiceAccountList.class.getSimpleName());
        accounts.values().forEach(sa -> list.addItemsItem(toListItem(sa)));
        return list;
    }

    public ServiceAccount reset(String id) {
        if (accounts.containsKey(id)) {
            LOGGER.info("Reset credentials for existing ServiceAccount {}", accounts.get(id));
            return setClientSecret(accounts.get(id));
        }
        LOGGER.info("Not found ServiceAccount {}", id);
        return null;
    }

    public ServiceAccount create(ServiceAccountRequest request, String basePath) {
        LOGGER.info("Creating new ServiceAccount {}", request);
        var owner = (String) token.claim("preferred_username").orElse("unknown");
        var serviceAccount = new ServiceAccount();
        serviceAccount.id(UUID.randomUUID().toString());
        serviceAccount.name(request.getName());
        serviceAccount.description(request.getDescription());
        serviceAccount.createdAt(ZonedDateTime.now().toOffsetDateTime());
        serviceAccount.setOwner(owner);
        serviceAccount.setHref(basePath + "/" + serviceAccount.getId());
        serviceAccount.setKind(SERVICE_ACCOUNT_KIND);
        if (!staticAccounts.containsKey(serviceAccount.getName())) {
            serviceAccount.setClientId("svc-acc" + rnd.nextInt());
        } else {
            var staticSa = staticAccounts.get(serviceAccount.getName());
            serviceAccount.setClientId(staticSa.getClientId());
        }
        setClientSecret(serviceAccount);
        accounts.put(serviceAccount.getId(), serviceAccount);
        return serviceAccount;
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
        item.setKind(SERVICE_ACCOUNT_KIND);
        item.setClientId(sa.getClientId());
        return item;
    }

    private ServiceAccount setClientSecret(ServiceAccount serviceAccount) {
        if (!staticAccounts.containsKey(serviceAccount.getName())) {
            serviceAccount.setClientSecret(UUID.randomUUID().toString());
        } else {
            var staticSa = staticAccounts.get(serviceAccount.getName());
            serviceAccount.setClientSecret(staticSa.getClientSecret());
        }
        return serviceAccount;
    }

    private ServiceAccount withoutCredentials(ServiceAccount sa) {
        var other = new ServiceAccount();
        other.setId(sa.getId());
        other.setName(sa.getName());
        other.setDescription(sa.getDescription());
        other.setCreatedAt(sa.getCreatedAt());
        other.setOwner(sa.getOwner());
        other.setHref(sa.getHref());
        other.setKind(SERVICE_ACCOUNT_KIND);
        other.setClientId(sa.getClientId());
        return other;
    }

}
