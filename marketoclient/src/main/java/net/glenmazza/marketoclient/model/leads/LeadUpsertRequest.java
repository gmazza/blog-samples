package net.glenmazza.marketoclient.model.leads;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public class LeadUpsertRequest<T extends LeadUpsertRecord> {

    private Action action = Action.CREATE_OR_UPDATE;

    private LookupField lookupField = LookupField.EMAIL;

    private List<T> input;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public LookupField getLookupField() {
        return lookupField;
    }

    public void setLookupField(LookupField lookupField) {
        this.lookupField = lookupField;
    }

    public List<T> getInput() {
        return input;
    }

    public void setInput(List<T> input) {
        this.input = input;
    }

    public enum Action {
        CREATE_OR_UPDATE("createOrUpdate"), CREATE_ONLY("createOnly"),
        UPDATE_ONLY("updateOnly"), CREATE_DUPLICATE("createDuplicate");

        private final String actionName;

        Action(String actionName) {
            this.actionName = actionName;
        }

        @JsonValue
        public String getActionName() {
            return actionName;
        }
    }

    public enum LookupField {
        EMAIL("email");

        private final String fieldName;

        LookupField(String fieldName) {
            this.fieldName = fieldName;
        }

        @JsonValue
        public String getFieldName() {
            return fieldName;
        }
    }
}
