/*
 * Copyright 2008 by Jon A. Webb
 *     This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the Lesser GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jjil.algorithm.j2se;

import jjil.algorithm.ErrorCodes;
import jjil.algorithm.j2se.Vec2;
import jjil.core.Error;
import jjil.core.Point;
import jjil.core.Triangle;

/**
 * Class manages a mapping from one triangle to another (an affine warp).<p>
 * @author webb
 */
public class TriangleMap {
    double A[][];
    Point p1, p2;
    
    /**
     * Create a new TriangleMap mapping points in one triangle into another.
     * @param t1 source triangle
     * @param t2 target triangle
     * @throws Error if some of the edges in t1 are of length zero
     */
    public TriangleMap(Triangle t1, Triangle t2) throws Error {
        this.p1 = t1.getP1();
        this.p2 = t2.getP1();
        Vec2 s12, s13, s22, s23;
        s12 = new Vec2(this.p1, t1.getP2());
        s13 = new Vec2(this.p1, t1.getP3());
        s22 = new Vec2(this.p2, t2.getP2());
        s23 = new Vec2(this.p2, t2.getP3());

        // The matrix transformation is
        // A vT = u
        // where vT is the original vector (s12 or s13), transposed, 
        // and u is the transformed vector (s22 or s23).
        // The solution to the transformation is
        // A = [s22T s23T][s12T s13T]-1 = [s22T s23T] B-1
        // Where -1 indicates matrix inversion of the 2x2 matrix (denoted B) formed from
        // the transposed vectors s12T and s13T.
        // Matrix inversion of a 2x2 matrix is easy.
        double detB = s12.getX() * s13.getY() - s13.getX() * s12.getY();
        if (detB == 0.0d) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.PARAMETER_OUT_OF_RANGE,
                            t1.toString(),
                            null,
                            null);
        }
        double Binv[][] = new double[2][2];
        Binv[0][0] = s13.getY() / detB;
        Binv[0][1] = -s13.getX() / detB;
        Binv[1][0] = -s12.getY() / detB;
        Binv[1][1] = s12.getX() / detB;
        // finally form A
        this.A = new double[2][2];
        this.A[0][0] = s22.getX() * Binv[0][0] + s23.getX() * Binv[1][0];
        this.A[0][1] = s22.getX() * Binv[0][1] + s23.getX() * Binv[1][1];
        this.A[1][0] = s22.getY() * Binv[0][0] + s23.getY() * Binv[1][0];
        this.A[1][1] = s22.getY() * Binv[0][1] + s23.getY() * Binv[1][1];
    }
    
    /**
     * Map point in one triangle into the other triangle
     * @param p Point to map
     * @return mapped Point
     */
    public Point map(Point p) {
        Vec2 v = new Vec2(p1, p);
        // multiply by A
        Vec2 v2 = new Vec2(
                this.A[0][0]*v.getX() + this.A[0][1]*v.getY(),
                this.A[1][0]*v.getX() + this.A[1][1]*v.getY());
        return v2.add(p2);
    }
}
