package net.glenmazza.sfclient.model;

public class AccountInsertCompositeRecord extends CompositeEntityRecord {

    private final Body body = new Body();
    public AccountInsertCompositeRecord(String referenceId) {
        super("Account", Method.POST, referenceId);
    }

    public Body getBody() {
        return body;
    }

    public static class Body {

        public String name;
        public int numberOfEmployees;
        public String site;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

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
