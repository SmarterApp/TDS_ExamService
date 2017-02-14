package tds.exam.builder;

import java.util.Arrays;
import java.util.List;

import tds.student.RtsStudentPackageAttribute;
import tds.student.RtsStudentPackageRelationship;
import tds.student.Student;

/**
 * Create a {@link tds.student.Student} with sample data
 */
public class StudentBuilder {
    private long id = 1L;
    private String loginSSID = "loginSSID";
    private String stateCode = "CA";
    private String clientName = "clientName";
    private tds.dll.common.rtspackage.student.data.Student studentPackage;
    private List<RtsStudentPackageAttribute> attributes = Arrays.asList(
        new RtsStudentPackageAttribute("UnitTestAttribute", "UnitTestAttributeValue"),
        new RtsStudentPackageAttribute("AnotherUnitTestAttribute", "AnotherUnitTestAttributeValue"));
    private List<RtsStudentPackageRelationship> relationships = Arrays.asList(
        new RtsStudentPackageRelationship("RelationshipId",
            "RelationshipType",
            "RelationshipValue",
            "entityKey"),
        new RtsStudentPackageRelationship("AnotherRelationshipId",
            "AnotherRelationshipType",
            "AnotherRelationshipValue",
            "anotherEntityKey"));

    public Student build() {
        return new Student.Builder(id, clientName)
            .withLoginSSID(loginSSID)
            .withStateCode(stateCode)
            .withAttributes(attributes)
            .withRelationships(relationships)
            .build();
    }

    public StudentBuilder withId(long id) {
        this.id = id;
        return this;
    }

    public StudentBuilder withLoginSSID(String loginSSID) {
        this.loginSSID = loginSSID;
        return this;
    }

    public StudentBuilder withStateCode(String stateCode) {
        this.stateCode = stateCode;
        return this;
    }

    public StudentBuilder withClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    public StudentBuilder withAttributes(List<RtsStudentPackageAttribute> attributes) {
        this.attributes = attributes;
        return this;
    }

    public StudentBuilder withRelationships(List<RtsStudentPackageRelationship> relationships) {
        this.relationships = relationships;
        return this;
    }
}
