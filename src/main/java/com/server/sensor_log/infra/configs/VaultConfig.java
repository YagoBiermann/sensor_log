package com.server.sensor_log.infra.configs;

import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions.RoleId;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions.SecretId;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.client.VaultClient;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.vault.config.EnvironmentVaultConfiguration;
import org.springframework.vault.support.VaultToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@PropertySource("vault.properties")
@Import(EnvironmentVaultConfiguration.class)
@Configuration
public class VaultConfig extends AbstractVaultConfiguration {
    @Value("${vault.uri}")
    public String vaultUri;

    @Value("${vault.pki-path}")
    public String pkiPath;

    @Value("${vault.app-role.role-id}")
    public String roleId;

    @Value("${vault.app-role.secret-id}")
    private String secretId;

    @Override
    public VaultEndpoint vaultEndpoint() {
        return VaultEndpoint.from(vaultUri);
    }

    @Override
    public ClientAuthentication clientAuthentication() {
        AppRoleAuthenticationOptions options = AppRoleAuthenticationOptions.builder()
                .roleId(RoleId.provided(roleId))
                .secretId(SecretId.wrapped(VaultToken.of(secretId)))
                .build();
                
        return new AppRoleAuthentication(options, VaultClient.create(vaultEndpoint()));
    }
}