package com.yoti.api.client.sandbox.profile.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yoti.api.attributes.AttributeConstants.HumanProfileAttributes;
import com.yoti.api.client.Date;
import com.yoti.api.client.DocumentDetails;
import com.yoti.api.client.sandbox.profile.request.attribute.SandboxAttribute;
import com.yoti.api.client.sandbox.profile.request.attribute.derivation.AgeVerification;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bouncycastle.util.encoders.Base64;

public class YotiTokenRequest {

    private final String rememberMeId;
    private final List<SandboxAttribute> sandboxAttributes;

    private YotiTokenRequest(String rememberMeId, List<SandboxAttribute> sandboxAttributes) {
        this.rememberMeId = rememberMeId;
        this.sandboxAttributes = sandboxAttributes;
    }

    @JsonProperty("remember_me_id")
    public String getRememberMeId() {
        return rememberMeId;
    }

    @JsonProperty("profile_attributes")
    public List<SandboxAttribute> getSandboxAttributes() {
        return sandboxAttributes;
    }

    public static YotiTokenRequestBuilder builder() {
        return new YotiTokenRequestBuilder();
    }

    public static class YotiTokenRequestBuilder {

        private YotiTokenRequestBuilder() {
        }

        private String rememberMeId;
        private final Map<String, SandboxAttribute> attributes = new HashMap<>();

        public YotiTokenRequestBuilder withRememberMeId(String value) {
            this.rememberMeId = value;
            return this;
        }

        public YotiTokenRequestBuilder withAttribute(SandboxAttribute sandboxAttribute) {
            attributes.put(sandboxAttribute.getName(), sandboxAttribute);
            return this;
        }

        public YotiTokenRequestBuilder withGivenNames(String value) {
            SandboxAttribute sandboxAttribute = createAttribute(HumanProfileAttributes.GIVEN_NAMES, value);
            return withAttribute(sandboxAttribute);
        }

        public YotiTokenRequestBuilder withFamilyName(String value) {
            SandboxAttribute sandboxAttribute = createAttribute(HumanProfileAttributes.FAMILY_NAME, value);
            return withAttribute(sandboxAttribute);
        }

        public YotiTokenRequestBuilder withFullName(String value) {
            SandboxAttribute sandboxAttribute = createAttribute(HumanProfileAttributes.FULL_NAME, value);
            return withAttribute(sandboxAttribute);
        }

        public YotiTokenRequestBuilder withDateOfBirth(Date value) {
            SandboxAttribute sandboxAttribute = createAttribute(HumanProfileAttributes.DATE_OF_BIRTH, value.toString());
            return withAttribute(sandboxAttribute);
        }

        public YotiTokenRequestBuilder withDateOfBirth(String value) {
            SandboxAttribute sandboxAttribute = createAttribute(HumanProfileAttributes.DATE_OF_BIRTH, value);
            return withAttribute(sandboxAttribute);
        }

        public YotiTokenRequestBuilder withAgeVerification(AgeVerification ageVerification) {
            return withAttribute(ageVerification.toAttribute());
        }

        public YotiTokenRequestBuilder withGender(String value) {
            SandboxAttribute sandboxAttribute = createAttribute(HumanProfileAttributes.GENDER, value);
            return withAttribute(sandboxAttribute);
        }

        public YotiTokenRequestBuilder withPhoneNumber(String value) {
            SandboxAttribute sandboxAttribute = createAttribute(HumanProfileAttributes.PHONE_NUMBER, value);
            return withAttribute(sandboxAttribute);
        }

        public YotiTokenRequestBuilder withNationality(String value) {
            SandboxAttribute sandboxAttribute = createAttribute(HumanProfileAttributes.NATIONALITY, value);
            return withAttribute(sandboxAttribute);
        }

        public YotiTokenRequestBuilder withPostalAddress(String value) {
            SandboxAttribute sandboxAttribute = createAttribute(HumanProfileAttributes.POSTAL_ADDRESS, value);
            return withAttribute(sandboxAttribute);
        }

        public YotiTokenRequestBuilder withStructuredPostalAddress(String value) {
            SandboxAttribute sandboxAttribute = createAttribute(HumanProfileAttributes.STRUCTURED_POSTAL_ADDRESS, value);
            return withAttribute(sandboxAttribute);
        }

        public YotiTokenRequestBuilder withSelfie(byte[] value) {
            String base64Selfie = Base64.toBase64String(value);
            return withBase64Selfie(base64Selfie);
        }

        public YotiTokenRequestBuilder withBase64Selfie(String value) {
            SandboxAttribute sandboxAttribute = createAttribute(HumanProfileAttributes.SELFIE, value);
            return withAttribute(sandboxAttribute);
        }

        public YotiTokenRequestBuilder withEmailAddress(String value) {
            SandboxAttribute sandboxAttribute = createAttribute(HumanProfileAttributes.EMAIL_ADDRESS, value);
            return withAttribute(sandboxAttribute);
        }

        public YotiTokenRequestBuilder withDocumentDetails(DocumentDetails value) {
            return withDocumentDetails(value.toString());
        }

        public YotiTokenRequestBuilder withDocumentDetails(String value) {
            SandboxAttribute sandboxAttribute  =  SandboxAttribute.builder()
                    .name(HumanProfileAttributes.DOCUMENT_DETAILS)
                    .value(value)
                    .optional(true)
                    .build();
            return withAttribute(sandboxAttribute);
        }

        public YotiTokenRequest build() {
            return new YotiTokenRequest(rememberMeId, Collections.unmodifiableList(new ArrayList<>(attributes.values())));
        }

        private SandboxAttribute createAttribute(String name, String value) {
            return SandboxAttribute.builder()
                    .name(name)
                    .value(value)
                    .build();
        }

    }

}