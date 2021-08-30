package bgu.spl.net.impl;

public class CourseNumOperation extends Operation{

    private int courseNum;

    public CourseNumOperation(short opcode, int courseNum) {
        super(opcode);
        this.courseNum = courseNum;
    }

    public int getCourseNum() {
        return courseNum;
    }
}
