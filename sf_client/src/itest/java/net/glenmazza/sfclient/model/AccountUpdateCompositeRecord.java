package net.glenmazza.sfclient.model;

public class AccountUpdateCompositeRecord extends CompositeEntityRecord {

    private final Body body = new Body();
    public AccountUpdateCompositeRecord(String referenceId) {
        super("Account", Method.PATCH, referenceId);
    }

    public Body getBody() {
        return body;
    }

    public static class Body {

        public int numberOfEmployees;
        public String site;

        public int getNumberOfEmployees() {
            return numberOfEmployees;
        }

        public void setNumberOfEmployees(int numberOfEmployees) {
            this.numberOfEmployees = numberOfEmployees;
        }

        public String getSite() {
            return site;
        }

        public void setSite(String site) {
            this.site = site;
        }
    }
}
