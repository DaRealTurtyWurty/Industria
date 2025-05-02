package dev.turtywurty.industria.blockentity.behaviourtree;

public abstract class ActionNode<T> extends BehaviourNode<T> {
    @Override
    public abstract Status tick(T context);
}
