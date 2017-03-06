/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Shows list of supported extensions providers.
 *
 * @author Pavel Bortnik
 */

@Component
public class ExtensionsInfoContributor implements InfoContributor {

    private static final String EXTENSION_KEY = "extension";

    private final DiscoveryClient discoveryClient;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public ExtensionsInfoContributor(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Set<String> collect = discoveryClient.getServices().stream().flatMap(service -> discoveryClient.getInstances(service).stream())
                .filter(instance -> instance.getMetadata().containsKey(EXTENSION_KEY))
                .map(instance -> instance.getMetadata().get(EXTENSION_KEY)).collect(Collectors.toCollection(TreeSet::new));
        builder.withDetail("extensions", collect);
    }
}
