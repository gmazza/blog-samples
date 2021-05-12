package net.glenmazza.cdclistener.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class AccountCDCEvent extends BaseCDCEvent {

    private Payload payload;

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload extends BasePayload {
        enum Rating { Hot, Warm, Cold }

        private String name;

        private Rating rating;

        private int numberOfEmployees;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Rating getRating() {
            return rating;
        }

        public void setRating(Rating rating) {
            this.rating = rating;
        }

        public int getNumberOfEmployees() {
            return numberOfEmployees;
        }

        public void setNumberOfEmployees(int numberOfEmployees) {
            this.numberOfEmployees = numberOfEmployees;
        }
    }

}
