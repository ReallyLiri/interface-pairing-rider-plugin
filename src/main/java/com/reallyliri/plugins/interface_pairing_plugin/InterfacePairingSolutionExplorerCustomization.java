package com.reallyliri.plugins.interface_pairing_plugin;

import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.jetbrains.rider.model.RdProjectFileDescriptor;
import com.jetbrains.rider.projectView.nodes.ProjectModelNode;
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerCustomization;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class InterfacePairingSolutionExplorerCustomization extends SolutionExplorerCustomization {
    private static final Logger log = Logger.getInstance(InterfacePairingSolutionExplorerCustomization.class);

    public InterfacePairingSolutionExplorerCustomization(@NotNull Project project) {
        super(project);
    }

    @NotNull
    @Override
    public List<AbstractTreeNode<?>> getChildren(@NotNull ProjectModelNode parentNode) {
        setInterfacePairingSortKeys(parentNode);
        return super.getChildren(parentNode); // always returns empty, but its fine
    }

    private void setInterfacePairingSortKeys(ProjectModelNode parentNode) {
        ArrayList<ProjectModelNode> children = parentNode.getChildren(true, false);
        Map<@NotNull String, ProjectModelNode> fileNodesByName = children.stream()
            .filter(node -> node.getDescriptor() instanceof RdProjectFileDescriptor)
            .collect(Collectors.toMap(ProjectModelNode::getName, node -> node));

        if (fileNodesByName.isEmpty()) {
            return;
        }

        Set<String> interfacesSet = fileNodesByName.keySet().stream()
            .filter(name -> name.endsWith(".cs"))
            .filter(name -> name.charAt(0) == 'I')
            .filter(name -> Character.isUpperCase(name.charAt(1)))
            .filter(name -> fileNodesByName.containsKey(name.substring(1)))
            .collect(Collectors.toSet());

        if (interfacesSet.isEmpty()) {
            return;
        }

        List<String> orderedNonInterfaceNames = fileNodesByName.keySet().stream()
            .filter(name -> !interfacesSet.contains(name))
            .sorted()
            .collect(Collectors.toList());

        int sortKey = 0;
        for (String currentNodeName : orderedNonInterfaceNames) {
            ProjectModelNode currentNode = fileNodesByName.get(currentNodeName);
            setNodeSortKey(currentNode, sortKey);
            sortKey++;

            String potentialInterfaceName = String.format("I%s", currentNodeName);
            if (interfacesSet.contains(potentialInterfaceName)) {
                ProjectModelNode interfaceNode = fileNodesByName.get(potentialInterfaceName);
                setNodeSortKey(interfaceNode, sortKey);
                sortKey++;
            }
        }

        assert sortKey == fileNodesByName.size();
    }

    private void setNodeSortKey(ProjectModelNode node, int sortKey) {
        RdProjectFileDescriptor fileDescriptor = (RdProjectFileDescriptor) node.getDescriptor();
        RdProjectFileDescriptor newDescriptor = new RdProjectFileDescriptor(
            fileDescriptor.isInternal(),
            fileDescriptor.isLinked(),
            fileDescriptor.getBuildAction(),
            sortKey,
            fileDescriptor.getUserData(),
            fileDescriptor.getName(),
            fileDescriptor.getLocation()
        );
        node.updateData(newDescriptor, node.getParent());
    }
}
