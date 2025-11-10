package toSTL;

public class Triangle {
    public final Vector3 normal;
    public final Vector3 v1, v2, v3;
    
    public Triangle(Vector3 v1, Vector3 v2, Vector3 v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.normal = calculateNormal();
    }
    
    public Triangle(Vector3 normal, Vector3 v1, Vector3 v2, Vector3 v3) {
        this.normal = normal;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }
    
    private Vector3 calculateNormal() {
        Vector3 edge1 = v2.subtract(v1);
        Vector3 edge2 = v3.subtract(v1);
        return edge1.cross(edge2).normalize();
    }
    
    @Override
    public String toString() {
        return String.format("Triangle[normal=%s, v1=%s, v2=%s, v3=%s]", 
            normal, v1, v2, v3);
    }
}
