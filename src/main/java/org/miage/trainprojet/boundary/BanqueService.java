package org.miage.trainprojet.boundary;

import org.miage.trainprojet.entity.*;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BanqueService {

    RestTemplate template;
    LoadBalancerClientFactory clientFactory;

    public BanqueService(RestTemplate template, LoadBalancerClientFactory clientFactory) {
        this.template = template;
        this.clientFactory = clientFactory;
    }

    public ReponseBanque appelServiceBanque(Reservation toUpdate){
        RoundRobinLoadBalancer lb = clientFactory.getInstance("banque-service", RoundRobinLoadBalancer.class);
        ServiceInstance instance = lb.choose().block().getServer();
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/clients/{idClient}/prix/{prix}/payer";

        return template.getForObject(url, ReponseBanque.class, toUpdate.getVoyageur().getId(), toUpdate.getPrix());
    }
}
