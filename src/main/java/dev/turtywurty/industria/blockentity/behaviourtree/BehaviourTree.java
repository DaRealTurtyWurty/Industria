package dev.turtywurty.industria.blockentity.behaviourtree;

public class BehaviourTree<T> {
    private final Blackboard blackboard;
    private BehaviourNode<T> root;

    public BehaviourTree() {
        this.blackboard = new Blackboard();
    }

    public void setRoot(BehaviourNode<T> root) {
        this.root = root;
        this.root.setBlackboard(blackboard);
    }

    public Blackboard getBlackboard() {
        return blackboard;
    }

    public void tick(T entity) {
        if (root != null) {
            root.tick(entity);
        }
    }
}
