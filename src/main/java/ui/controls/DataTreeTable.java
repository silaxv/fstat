package ui.controls;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class DataTreeTable extends JScrollPane {

    private final JXTreeTable treeTable;
    private final DataTreeTableModel tableModel;

    public static class DataTreeTableNode extends AbstractMutableTreeTableNode {

        private String name;
        private Integer id;
        private final List<DataTreeTableNode> children = new ArrayList<>();

        public DataTreeTableNode(String name, Integer id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public List<DataTreeTableNode> getChildren() {
            return children;
        }

        public String toString() {
            return "TreeNode: " + name;
        }

        @Override
        public Object getValueAt(int i) {
            return "null";
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

    }

    public static class DataTreeTableModel extends AbstractTreeTableModel {

        private final DataTreeTableNode root;
        private final String[] headerCaptions;

        public DataTreeTableModel(String[] headerCaptions) {
            this.headerCaptions = headerCaptions;
            root = new DataTreeTableNode("root", null);
            root.getChildren().add(new DataTreeTableNode("Empty", null));
        }

        @Override
        public int getColumnCount() {
            return headerCaptions.length;
        }

        @Override
        public String getColumnName(int column) {
            if (column < headerCaptions.length) {
                return headerCaptions[column];
            }

            return "Unknown";
        }

        @Override
        public Object getValueAt(Object node, int column) {
            DataTreeTableNode treeNode = (DataTreeTableNode) node;
            if (column == 0) {
                return treeNode.getName();
            }

            return "Unknown";
        }

        @Override
        public Object getChild(Object node, int index) {
            DataTreeTableNode treeNode = (DataTreeTableNode) node;

            return treeNode.getChildren().get(index);
        }

        @Override
        public int getChildCount(Object parent) {
            DataTreeTableNode treeNode = (DataTreeTableNode) parent;

            return treeNode.getChildren().size();
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            DataTreeTableNode treeNode = (DataTreeTableNode) parent;
            for (int i = 0; i < treeNode.getChildren().size(); i++) {
                if (treeNode.getChildren().get(i) == child) {
                    return i;
                }
            }

            return 0;
        }

        public boolean isLeaf(Object node) {
            DataTreeTableNode treeNode = (DataTreeTableNode) node;

            return treeNode.getChildren().isEmpty();
        }

        @Override
        public DataTreeTableNode getRoot() {
            return root;
        }

    }

    public DataTreeTable() {
        treeTable = new JXTreeTable();

        setViewportView(treeTable);

        tableModel = new DataTreeTableModel(new String[]{"Category"});
        treeTable.setTreeTableModel(tableModel);
        treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        treeTable.setShowVerticalLines(false);
        treeTable.setShowHorizontalLines(false);

        tableModel.getRoot().getChildren().clear();
    }

    public void addMouseListener(MouseListener listener) {
        treeTable.addMouseListener(listener);
    }

    public DataTreeTableNode addNode(int id, String name, DataTreeTableNode parent) {
        DataTreeTableNode parentNode = parent == null ? tableModel.getRoot() : parent;
        DataTreeTableNode newNode = new DataTreeTableNode(name, id);
        parentNode.getChildren().add(newNode);

        return newNode;
    }

    public void update() {
        treeTable.updateUI();
    }

    public Integer getSelectedItemId() {
        TreePath path = treeTable.getPathForRow(treeTable.getSelectedRow());
        if (path == null)
            return null;

        DataTreeTableNode node = (DataTreeTableNode) path.getLastPathComponent();

        return node != null ? node.getId() : null;
    }

    public void setSelectedRow(Integer id) {
        if (id == null)
            return;

        Object[] nodes = getNodePathById(id, tableModel.getRoot().getChildren());
        if (nodes != null) {
            Object[] nodesForSelect = new Object[nodes.length + 1];
            Object[] nodesForExpand = new Object[nodes.length];
            nodesForSelect[0] = tableModel.getRoot();
            System.arraycopy(nodes, 0, nodesForSelect, 1, nodes.length);
            System.arraycopy(nodesForSelect, 0, nodesForExpand, 0, nodesForExpand.length);
            treeTable.expandPath(new TreePath(nodesForExpand));
            var row = treeTable.getRowForPath(new TreePath(nodesForSelect));
            treeTable.setRowSelectionInterval(row, row);
        }
    }

    private Object[] getNodePathById(int id, List<DataTreeTableNode> children) {
        for (var node : children) {
            if (node.getId() == id)
                return new Object[] { node };
            Object[] childNodes = getNodePathById(id, node.getChildren());
            if (childNodes != null) {
                Object[] currentNodes = new Object[childNodes.length + 1];
                currentNodes[0] = node;
                System.arraycopy(childNodes, 0, currentNodes, 1, childNodes.length);
                return currentNodes;
            }
        }

        return null;
    }

}
