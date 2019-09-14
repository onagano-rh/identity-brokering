# export SSO_HOME=/path/to/rh-sso-7.3
# export EAP_HOME=/path/to/jboss-eap-7.2

JAVA_OPTS="-Xmx256m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true"


# RH-SSO IdB server, port-offset 100 (listen 8180)

function idb-server() {
    JBOSS_HOME=${SSO_HOME} JAVA_OPTS=${JAVA_OPTS} \
              ${SSO_HOME}/bin/standalone.sh \
              -Djboss.server.base.dir=./standalone.idb \
              -b 0.0.0.0 \
              -Djboss.socket.binding.port-offset=100 \
              $@
}

function idb-cli() {
    ${SSO_HOME}/bin/jboss-cli.sh --controller=localhost:10090 -c "$@"
}


# EAP app server, port-offset 200 (listen 8280)

function app-server() {
    JBOSS_HOME=${EAP_HOME} JAVA_OPTS=${JAVA_OPTS} \
              ${EAP_HOME}/bin/standalone.sh \
              -Djboss.server.base.dir=./standalone.app \
              -b 0.0.0.0 \
              -Djboss.socket.binding.port-offset=200 \
              $@
}

function app-cli() {
    ${EAP_HOME}/bin/jboss-cli.sh --controller=localhost:10190 -c "$@"
}


# RH-SSO IdP server, port-offset 300 (listen 8380)

function idp-server() {
    JBOSS_HOME=${SSO_HOME} JAVA_OPTS=${JAVA_OPTS} \
              ${SSO_HOME}/bin/standalone.sh \
              -Djboss.server.base.dir=./standalone.idp \
              -b 0.0.0.0 \
              -Djboss.socket.binding.port-offset=300 \
              $@
}

function idp-cli() {
    ${SSO_HOME}/bin/jboss-cli.sh --controller=localhost:10290 -c "$@"
}


# EAP service server, port-offset 400 (listen 8480)

function svc-server() {
    JBOSS_HOME=${EAP_HOME} JAVA_OPTS=${JAVA_OPTS} \
              ${EAP_HOME}/bin/standalone.sh \
              -Djboss.server.base.dir=./standalone.svc \
              -b 0.0.0.0 \
              -Djboss.socket.binding.port-offset=400 \
              $@
}

function svc-cli() {
    ${EAP_HOME}/bin/jboss-cli.sh --controller=localhost:10390 -c "$@"
}


# Build and deploy app-jee-jsp

function deploy-app() {
    (
        cd app-jee-jsp
        mvn -Denforcer.skip=true clean package
        cp target/app-jsp.war ../standalone.app/deployments/
    )
}


# Build and deploy service-jee-jaxrs

function deploy-svc() {
    (
        cd service-jee-jaxrs
        mvn -Denforcer.skip=true clean package
        cp target/service.war ../standalone.app/deployments/
    )
}
