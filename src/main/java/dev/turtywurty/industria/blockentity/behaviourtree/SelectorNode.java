package dev.turtywurty.industria.blockentity.behaviourtree;

import java.util.ArrayList;
import java.util.List;

public class SelectorNode<T> extends BehaviourNode<T> {
    private final List<BehaviourNode<T>> children = new ArrayList<>();

    public void addChild(BehaviourNode<T> child) {
        children.add(child);
        child.setBlackboard(blackboard);
    }

    @Override
    public Status tick(T context) {
        for (BehaviourNode<T> child : children) {
            Status status = child.tick(context);
            if (status != Status.FAILURE) {
                return status;
            }
        }

        return Status.FAILURE;
    }
}
