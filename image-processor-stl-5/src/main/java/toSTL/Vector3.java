package toSTL;

public class Vector3 {
    public final float x, y, z;
    
    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Vector3 subtract(Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }
    
    public Vector3 cross(Vector3 other) {
        return new Vector3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }
    
    public Vector3 normalize() {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        if (length == 0) return new Vector3(0, 0, 1);
        return new Vector3(x / length, y / length, z / length);
    }
    
    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
}
