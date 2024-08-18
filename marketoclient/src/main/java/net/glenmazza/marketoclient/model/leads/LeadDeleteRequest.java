package net.glenmazza.marketoclient.model.leads;

import java.util.List;

/**
 * Deleting Leads:
 * <a href="https://developers.marketo.com/rest-api/lead-database/leads/#delete">Marketo Docs</a>
 */
public class LeadDeleteRequest {

    List<Input> input;

    public List<Input> getInput() {
        return input;
    }

    public void setInput(List<Input> input) {
        this.input = input;
    }

    public static class Input {
        Integer id;

        public Input(Integer id) {
            this.id = id;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }

}
