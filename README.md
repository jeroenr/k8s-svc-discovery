# K8s service discovery hook [![Build Status](https://travis-ci.org/jeroenr/k8s-svc-discovery.svg?branch=master)](https://travis-ci.org/jeroenr/k8s-svc-discovery) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.jeroenr/k8s-svc-discovery_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.jeroenr/k8s-svc-discovery_2.11)
Simple library which implements an agent that listens for Kubernetes svc events and provides a callback to receive service metadata. It's polling the [Kubernetes API endpoint](https://kubernetes.io/docs/api-reference/v1.7/#list-all-namespaces-162) ```/api/v1/services``` endpoint for services

## Contributions
This is a young but very active project and absolutely needs your help. Good ways to contribute include:

* Raising bugs and feature requests
* Fixing bugs
* Improving the performance
* Adding to the documentation

## Quick start

### Setup
Latest stable release is **0.6** and is available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Ck8s-svc-discovery). Just add the following dependency:

```scala
libraryDependencies ++= Seq(
  "com.github.jeroenr" %% "k8s-svc-discovery" % "0.6"
)
```

Or if you want to be on the bleeding edge using snapshots, latest snapshot release is **0.7-SNAPSHOT**. Add the following repository and dependency:
```scala
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "com.github.jeroenr" %% "k8s-svc-discovery" % "0.7-SNAPSHOT"
)
```

### Usage
You can simply initiate the ```ServiceDiscoveryAgent``` actor and pass an instance of ```KubernetesServiceDiscoveryClient``` along with your callback function of signature ```(List[T <: ServiceUpdate]) => Any```
```scala
import com.github.jeroenr.service.discovery._
import com.github.jeroenr.service.discovery.health._

implicit val system = ActorSystem()

// some callback function to do something useful with the list of service updates
def handleServiceUpdates[T <: ServiceUpdate](allServiceUpdates: List[T]) = {
  println(s"Service updates: $allServiceUpdates")
}

val serviceDiscoveryAgent =
    system.actorOf(Props(new ServiceDiscoveryAgent[KubernetesServiceUpdate](new KubernetesServiceDiscoveryClient, handleServiceUpdates)))

  // start watching services
  serviceDiscoveryAgent ! ServiceDiscoveryAgent.WatchServices
```
Example usage in a real project: https://github.com/jeroenr/api-gateway#automatic-service-discovery

### Configuration
When using this library you need to configure the Kubernetes API endpoint. 
```
service-discovery {
  kubernetes {
    host = "localhost"
    host = ${?K8S_API_HOST}
    port = 8001 // default kube-proxy port
    port = ${?K8S_API_PORT}
    token = ""
    token = ${?K8S_API_TOKEN}  // you need a valid API token to access the Kubernetes API 
    # namespaces that are filtered down to in the client
    namespaces = ["default"] // list of namespaces to monitor
  }

  polling.interval = 2 seconds
}
```
Most important is to set the Kubernetes API token. Conveniently you can easily do all this through environment variables in your Kubernetes deployment descriptor.
### Authenticating K8s API calls
You need a valid API token to access the Kubernetes API. There's many [different options](https://kubernetes.io/docs/admin/authentication/) for this. My favourite is through [service accounts](https://kubernetes.io/docs/admin/authentication/#service-account-tokens). The steps below will create a service account and associated [secret](https://kubernetes.io/docs/concepts/configuration/secret/)
```bash
$ kubectl create serviceaccount my-service-account
serviceaccount "my-service-account" created
```
Great, now a new service account has been created and under the hood also an associated secret which we can retrieve by:
```bash
$ kubectl get serviceaccounts my-service-account -o yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  # ...
secrets:
- name: my-service-account-token-1yvwg
```
As you can see in our example the generated secret is called "my-service-account-token-1yvwg". You can use this secret to set the value of the ```K8S_API_TOKEN``` environment variable. Example K8s deployment descriptor:
```yaml
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: my-deployment
spec:
  template:
    spec:
      containers:
      - name: my-container
        image: my-container-image
        env:
        - name: K8S_API_TOKEN
          valueFrom:
            secretKeyRef:
              name: my-serviceaccount-token-secret
              key: token
        - name: K8S_API_HOST
          value: "kubernetes.default.svc.cluster.local"
        - name: K8S_API_PORT
          value: "443"
        args: [
          "-Djava.security.egd=file:/dev/./urandom",
          "-Dintegration.kubernetes.namespaces.0=my-env",
        ]
```
