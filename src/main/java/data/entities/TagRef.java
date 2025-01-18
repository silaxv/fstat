package data.entities;

/*
    TABLE tags_refs
        document_detail_id INT
        tag_id INT
*/

public class TagRef {

    public static final String TABLE_R_NAME = "tags_refs";
    public static final String COLUMN_DOCUMENT_DETAIL = "document_detail_id";
    public static final String COLUMN_TAG = "tag_id";

    private Tag tag;
    private Document document; //Additional link to a document that is not stored in DB
    private DocumentDetail documentDetail;

    public TagRef(Tag tag, DocumentDetail document_detail) {
        this.tag = tag;
        this.documentDetail = document_detail;
    }

    public TagRef(Tag tag, Document document) {
        this.tag = tag;
        this.document = document;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public DocumentDetail getDocumentDetail() {
        return documentDetail;
    }

    public void setDocumentDetail(DocumentDetail documentDetail) {
        this.documentDetail = documentDetail;
    }

}
