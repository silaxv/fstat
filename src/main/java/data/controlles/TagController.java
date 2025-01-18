package data.controlles;

import data.DataManager;
import data.entities.Document;
import data.entities.DocumentDetail;
import data.entities.Tag;
import data.entities.TagRef;
import utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static data.entities.Document.*;
import static data.entities.DocumentDetail.*;
import static data.entities.Tag.*;
import static data.entities.TagRef.*;

public class TagController extends EntityController<Tag> {

    public TagController(DataManager dataManager) {
        super(dataManager, Tag.TABLE_NAME);
    }

    public void createTable() throws Exception {
        //TABLE tags
        dbController.createTable(Tag.TABLE_NAME, new String[] {
                Tag.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL",
                COLUMN_NAME + " VARCHAR(64) NOT NULL"
        });

        //TABLE tags_refs
        dbController.createTable(TABLE_R_NAME, new String[] {
                COLUMN_DOCUMENT_DETAIL + " INTEGER NOT NULL",
                COLUMN_TAG + " INT NOT NULL",
                "UNIQUE(" + COLUMN_DOCUMENT_DETAIL + ", " + COLUMN_TAG + ")"
        });
    }

    public void dropTable() throws Exception {
        super.dropTable();
        if (dbController.tableExists(TABLE_R_NAME))
            dbController.dropTable(TABLE_R_NAME);
    }

    public Tag getTagById(int id) {
        for (var i = 0; i < size(); i++) {
            Tag tag = get(i);
            if (tag.getId() == id) {
                return tag;
            }
        }

        return null;
    }

    public void loadTagsList() throws Exception {
        loadList(null, COLUMN_NAME);
    }

    public Tag loadTagById(int id) throws Exception {
        return loadItem("id=" + id);
    }

    public int addTag(String name) throws Exception {
        return addItem(new String[] { COLUMN_NAME } , new String[] { name });
    }

    public boolean updateTag(int id, String name) throws Exception {
        return updateItem(Tag.COLUMN_ID + "=" + id, new String[] { COLUMN_NAME } , new String[] { name });
    }

    public boolean deleteTag(int id) throws Exception {
        return deleteItem(Tag.COLUMN_ID + "=" + id);
    }

    public List<TagRef> loadDocumentsTags(DocumentController documents, Date date0, Date date1) throws Exception {
        List<TagRef> tagsRefs = new ArrayList<>();

        try {
            //Query returns: [0] tag_id, [1] document_id
            var sql = "SELECT DISTINCT tr." + TagRef.COLUMN_TAG + ", d." + Document.COLUMN_ID + " FROM " + Document.TABLE_NAME + " AS d\n" +
                    "LEFT JOIN " + TABLE_DT_NAME + " AS dd ON dd." + COLUMN_DT_DOCUMENT + " = d." + Document.COLUMN_ID + "\n" +
                    "LEFT JOIN " + TagRef.TABLE_R_NAME + " AS tr ON tr." + TagRef.COLUMN_DOCUMENT_DETAIL + " = dd." + COLUMN_DT_ID + "\n" +
                    "WHERE d." + COLUMN_DATE + " BETWEEN " + DateUtils.dateToInt(date0) + " AND " + DateUtils.dateToInt(date1) + "\n" +
                    "AND tr." + TagRef.COLUMN_TAG + " IS NOT NULL";
            List<Object[]> buffer = dbController.execQuery(sql);
            for (var item : buffer) {
                TagRef tagRef = new TagRef(
                        item[0] != null ? getTagById((int) item[0]) : null,
                        item[1] != null ? documents.getDocumentById((int) item[1]) : null
                );
                if (tagRef.getTag() == null || tagRef.getDocument() == null)
                    throw new Exception("Tag reference can't contain NULL (tag=" + tagRef.getTag() + ", doc=" + tagRef.getDocument() + ")");
                tagsRefs.add(tagRef);
            }
        } catch (Exception e) {
            throw new Exception("Can't load documents tags\n" + e.getMessage());
        }

        return tagsRefs;
    }

    public List<TagRef> loadDocumentTags(Document document, List<DocumentDetail> documentDetails) throws Exception {
        List<TagRef> tagsRefs = new ArrayList<>();

        try {
            //Query returns: [0] document_detail_id, [1] tag_id
            var sql = "SELECT dd." + COLUMN_DT_ID + ", tr." + TagRef.COLUMN_TAG + "\n" +
                    "FROM " + TABLE_DT_NAME + " AS dd\n" +
                    "INNER JOIN " + TagRef.TABLE_R_NAME + " AS tr ON tr." + TagRef.COLUMN_DOCUMENT_DETAIL + " = dd." + COLUMN_DT_ID + "\n" +
                    "WHERE dd." + COLUMN_DT_DOCUMENT + " = " + document.getId();
            List<Object[]> buffer = dbController.execQuery(sql);
            for (var item : buffer) {
                TagRef tagRef = new TagRef(
                        item[1] != null ? getTagById((int) item[1]) : null,
                        DocumentController.getDocumentDetail((Integer) item[0], documentDetails)
                );
                if (tagRef.getTag() == null)
                    throw new Exception("Tag reference can't be NULL");
                tagsRefs.add(tagRef);
            }
        } catch (Exception e) {
            throw new Exception("Can't load document detail tags\n" + e.getMessage());
        }

        return tagsRefs;
    }

    public void deleteTagsRefsByDocumentId(int id) throws Exception {
        var condition = COLUMN_DOCUMENT_DETAIL + " IN (SELECT id FROM documents_details WHERE document_id = " + id + ")";
        deleteTagsRefs(condition);
    }

    public void deleteTagsRefs(String condition) throws Exception {
        try {
            dbController.deleteData(TABLE_R_NAME, condition);
        } catch (Exception e) {
            throw new Exception("Can't delete items (" + condition + ")\n" + e.getMessage());
        }
    }

    public void addTags(List<TagRef> tagsRefs) throws Exception {
        if (tagsRefs == null || tagsRefs.isEmpty())
            return;

        //Prepare data for insertion
        var columnsNames = new String[] {
                COLUMN_DOCUMENT_DETAIL,
                COLUMN_TAG
        };
        var columnsValues = new ArrayList<Object[]>();
        for (var tagRef : tagsRefs) {
            if (tagRef.getDocumentDetail() == null || tagRef.getTag() == null)
                continue;
            var row = new Object[] {
                    tagRef.getDocumentDetail().getId(),
                    tagRef.getTag().getId()
            };
            columnsValues.add(row);
        }

        //Insert records
        try {
            dbController.insertDataBatch(TABLE_R_NAME, columnsNames, columnsValues);
        } catch (Exception e) {
            throw new Exception("Can't create a new tag reference\n" + e.getMessage());
        }
    }

    public void updateTags(DocumentDetail documentDetail, List<Tag> tags) throws Exception {
        if (documentDetail == null || documentDetail.getId() == null)
            return;

        //Read current tags
        List<Integer> oldTagsList = new ArrayList<>();
        try {
            //Query returns: [1] tag_id
            var sql = "SELECT tr." + TagRef.COLUMN_TAG + "\n" +
                    "FROM " + TABLE_DT_NAME + " AS dd\n" +
                    "INNER JOIN " + TagRef.TABLE_R_NAME + " AS tr ON tr." + TagRef.COLUMN_DOCUMENT_DETAIL + " = dd." + COLUMN_DT_ID + "\n" +
                    "WHERE dd." + COLUMN_DT_ID + " = " + documentDetail.getId();
            List<Object[]> buffer = dbController.execQuery(sql);
            for (var item : buffer) {
                Tag tag = item[0] != null ? getTagById((int) item[0]) : null;
                if (tag == null)
                    throw new Exception("Tag reference can't be NULL");
                oldTagsList.add(tag.getId());
            }
        } catch (Exception e) {
            throw new Exception("Can't load document detail tags\n" + e.getMessage());
        }

        //Compare lists
        boolean rewriteTags = tags.size() != oldTagsList.size();
        if (!rewriteTags) {
            for (var tag : tags) {
                boolean tagFound = false;
                for (var oldTagId : oldTagsList) {
                    if (tag.getId() == oldTagId) {
                        tagFound = true;
                        break;
                    }
                }
                if (!tagFound) {
                    rewriteTags = true;
                    break;
                }
            }
        }

        //Rewrite tags if needed
        if (rewriteTags) {
            System.out.println("TAGS UPD: " + documentDetail.getId() + " => " + Tag.tagsListToString(tags));
            //Delete tags refs
            var condition = COLUMN_DOCUMENT_DETAIL + " = " + documentDetail.getId();
            deleteTagsRefs(condition);
            //Insert tags refs
            List<TagRef> tagsRefs = new ArrayList<>();
            for (var tag : tags) {
                tagsRefs.add(new TagRef(tag, documentDetail));
            }
            addTags(tagsRefs);
        }
    }

    protected List<Object[]> selectItems(String condition, String orderBy) throws Exception {
        List<Object[]> buffer;

        var sql = "SELECT " + Tag.COLUMN_ID + ", " + COLUMN_NAME + " FROM " + Tag.TABLE_NAME
                + (condition != null && !condition.isEmpty() ? " WHERE " + condition : "")
                + (orderBy != null && !orderBy.isEmpty() ? " ORDER BY " + orderBy : "");
        buffer = dbController.execQuery(sql);

        return buffer;
    }

    protected Tag bufferToItem(Object[] row) {
        Tag item;
        item = new Tag();

        item.setId((int) row[0]);
        item.setName((String) row[1]);

        return item;
    }
}
