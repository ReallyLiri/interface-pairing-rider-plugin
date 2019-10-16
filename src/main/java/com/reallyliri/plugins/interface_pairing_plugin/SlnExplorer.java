package com.reallyliri.plugins.interface_pairing_plugin;

import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.jetbrains.rider.model.RdProjectFileDescriptor;
import com.jetbrains.rider.model.RdProjectModelItemDescriptor;
import com.jetbrains.rider.projectView.nodes.ProjectModelNode;
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerCustomization;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class SlnExplorer extends SolutionExplorerCustomization {
    private static final Logger log = Logger.getInstance(SlnExplorer.class);


    public SlnExplorer(@NotNull Project project) {
        super(project);
    }

    @NotNull
    @Override
    public List<AbstractTreeNode<?>> getChildren(@NotNull ProjectModelNode node) {
        ArrayList<ProjectModelNode> children1 = node.getChildren(true, false);
        children1.forEach(child -> {
            RdProjectModelItemDescriptor descriptor = child.getDescriptor();
            if (!(descriptor instanceof RdProjectFileDescriptor) || ((RdProjectFileDescriptor) descriptor).getSortKey() != null) {
                return;
            }
            RdProjectFileDescriptor fileDescriptor = (RdProjectFileDescriptor) descriptor;
            RdProjectFileDescriptor newDescriptor = new RdProjectFileDescriptor(fileDescriptor.isInternal(), fileDescriptor.isLinked(), fileDescriptor.getBuildAction(),
                1000 - (int) fileDescriptor.getName().charAt(0), fileDescriptor.getUserData(), fileDescriptor.getName(), fileDescriptor.getLocation());
            child.updateData(newDescriptor, node);
        });
        return super.getChildren(node);
    }
}