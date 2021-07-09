package net.glenmazza.sflistener.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountAndContactsPlatformEvent {

    private Payload payload;

    public Payload getPayload() {
        return payload;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {

        @JsonProperty("AccountSFID__C")
        private String id;
        @JsonProperty("AccountName__C")
        private String name;

        // Due to Platform Events limitation, message will contain contacts in
        // a (JSON) String instead of a List of Contact objects.
        // Responsibility of code in AccountAndContactsProcessor to parse them
        // into Contact objects
        @JsonProperty("Contacts__c")
        private String contactsString;

        // This set will be populated during processing from the contents in
        // contactsString
        @JsonIgnore
        private List<Contact> contacts;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContactsString() {
            return contactsString;
        }

        public void setContactsString(String contactsString) {
            this.contactsString = contactsString;
        }

        public List<Contact> getContacts() {
            if (contacts == null) {
                contacts = new ArrayList<>();
            }
            return contacts;
        }
    }

    public static class Contact {
        private String userId;
        private String emailAddress;
        private String firstName;
        private String lastName;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getEmailAddress() {
            return emailAddress;
        }

        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
}
