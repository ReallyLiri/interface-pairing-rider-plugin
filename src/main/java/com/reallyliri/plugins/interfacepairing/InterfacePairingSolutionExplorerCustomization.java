package com.reallyliri.plugins.interfacepairing;

import com.google.common.collect.Streams;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.rider.model.RdProjectFileDescriptor;
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerCustomization;
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity;
import java.awt.EventQueue;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.NotNull;

public class InterfacePairingSolutionExplorerCustomization extends SolutionExplorerCustomization {
    private static final Logger log = Logger.getInstance(InterfacePairingSolutionExplorerCustomization.class);

    public InterfacePairingSolutionExplorerCustomization(@NotNull Project project) {
        super(project);
    }

    @Override
    public int compareNodes(@NotNull ProjectModelEntity x, @NotNull ProjectModelEntity y) {
        return -1 * super.compareNodes(x, y);
    }

    @Override
    public void updateNode(@NotNull PresentationData presentation, @NotNull VirtualFile virtualFile) {
        super.updateNode(presentation, virtualFile);
    }

    @Override
    public void updateNode(@NotNull PresentationData presentation, @NotNull ProjectModelEntity entity) {
        super.updateNode(presentation, entity);
    }

    @NotNull
    @Override
    public List<AbstractTreeNode<?>> getChildren(@NotNull ProjectModelEntity entity) {
        if (!EventQueue.isDispatchThread()) {
            setInterfacePairingSortKeys(entity);
        }
        return super.getChildren(entity); // always returns empty, but its fine
    }

    private void setInterfacePairingSortKeys(ProjectModelEntity entity) {
        Sequence<ProjectModelEntity> children = entity.getChildrenEntities();
        Map<@NotNull String, ProjectModelEntity> fileNodesByName =
            Streams.stream(children.iterator())
                .filter(node -> node.getDescriptor() instanceof RdProjectFileDescriptor)
                .collect(Collectors.toMap(ProjectModelEntity::getName, node -> node, (node1, node2) -> node1));

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
            ProjectModelEntity currentNode = fileNodesByName.get(currentNodeName);
            setNodeSortKey(currentNode, sortKey);
            sortKey++;

            String potentialInterfaceName = String.format("I%s", currentNodeName);
            if (interfacesSet.contains(potentialInterfaceName)) {
                ProjectModelEntity interfaceNode = fileNodesByName.get(potentialInterfaceName);
                setNodeSortKey(interfaceNode, sortKey);
                sortKey++;
            }
        }

        assert sortKey == fileNodesByName.size();
    }

    private void setNodeSortKey(ProjectModelEntity node, int sortKey) {
        RdProjectFileDescriptor fileDescriptor = (RdProjectFileDescriptor) node.getDescriptor();
        if (fileDescriptor.getSortKey() != null && fileDescriptor.getSortKey() == sortKey) {
            return;
        }

        try {
            Field sortKeyField = fileDescriptor.getClass().getDeclaredField("sortKey");
            sortKeyField.setAccessible(true);
            sortKeyField.set(fileDescriptor, sortKey);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
