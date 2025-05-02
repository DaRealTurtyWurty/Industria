package dev.turtywurty.industria.blockentity.behaviourtree;

public abstract class BehaviourNode<T> {
    protected Blackboard blackboard;

    public BehaviourNode() {
        this.blackboard = new Blackboard();
    }

    public void setBlackboard(Blackboard blackboard) {
        this.blackboard = blackboard;
    }

    public abstract Status tick(T context);
}