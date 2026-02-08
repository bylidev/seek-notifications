module co.seekglobal.notification {
    exports co.seekglobal.notification.domain;
    exports co.seekglobal.notification.application.inbound;
    exports co.seekglobal.notification.application.outbound;
    exports co.seekglobal.notification.application.usecase;
    exports co.seekglobal.notification.infrastructure.config;
    exports co.seekglobal.notification.infrastructure.adapters.email;
    exports co.seekglobal.notification.infrastructure.adapters.sms;
    exports co.seekglobal.notification.infrastructure.adapters.push;

    requires static lombok;
    requires redisson;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    requires jakarta.validation;
}