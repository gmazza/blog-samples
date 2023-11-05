package net.glenmazza.marketoclient.marketoclient.model.leads;

/**
 * For Lead upserting into Marketo, defines the fields being sent to Marketo
 * <a href="https://developers.marketo.com/rest-api/lead-database/leads/#create_and_update">Marketo Docs</a>
 * Subclassed and used as part of the LeadUpsertRequest.
 * Usage:
 * 1.) Subclass and add fields for each field specified in the upsert.
 * 2.) Add @JsonIgnoreProperties(ignoreUnknown = true) if there are columns being returned that
 *     you haven't provided fields for.
 * Note id field cannot be provided on a create.
 */
public abstract class LeadUpsertRecord {
}
