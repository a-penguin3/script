public class ChildClass extends ParentClass {
    private int childX = 1;

    public ChildClass() {
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        childX = x;
        System.out.println("ChildX 被赋值为 " + x);
    }

    public void printX() {
        System.out.println("ChildX = " + childX);
    }

}
