/*
 * Copyright (C) 2012 René Jeschke <rene_jeschke@yahoo.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rjeschke.neetutils.vectors;

/**
 *
 * @author René Jeschke (rene_jeschke@yahoo.de)
 *
 */
public class Matrix4x4d
{
    // column major order
    public final static int        M00      = 0 * 4 + 0;
    public final static int        M01      = 1 * 4 + 0;
    public final static int        M02      = 2 * 4 + 0;
    public final static int        M03      = 3 * 4 + 0;
    public final static int        M10      = 0 * 4 + 1;
    public final static int        M11      = 1 * 4 + 1;
    public final static int        M12      = 2 * 4 + 1;
    public final static int        M13      = 3 * 4 + 1;
    public final static int        M20      = 0 * 4 + 2;
    public final static int        M21      = 1 * 4 + 2;
    public final static int        M22      = 2 * 4 + 2;
    public final static int        M23      = 3 * 4 + 2;
    public final static int        M30      = 0 * 4 + 3;
    public final static int        M31      = 1 * 4 + 3;
    public final static int        M32      = 2 * 4 + 3;
    public final static int        M33      = 3 * 4 + 3;
    protected final double[]       data     = new double[16];

    public final static Matrix4x4d IDENTITY = Matrix4x4d.identity();

    private Matrix4x4d()
    {
        //
    }

    public Matrix4x4d(final double[] m, final boolean transpose)
    {
        if (transpose)
        {
            for (int y = 0; y < 4; y++)
            {
                for (int x = 0; x < 4; x++)
                {
                    this.data[y * 4 + x] = m[x * 4 + y];
                }
            }
        }
        else
        {
            for (int i = 0; i < 16; i++)
                this.data[i] = m[i];
        }
    }

    public Matrix4x4d(final double[] m)
    {
        this(m, false);
    }

    public Matrix4x4d(final double[] m, final int offset)
    {
        System.arraycopy(m, offset, this.data, 0, 16);
    }

    private static Matrix4x4d identity()
    {
        final Matrix4x4d mat = new Matrix4x4d();
        mat.data[M00] = 1.0;
        mat.data[M11] = 1.0;
        mat.data[M22] = 1.0;
        mat.data[M33] = 1.0;
        return mat;
    }

    public double determinant()
    {
        final double s0 = this.data[M00] * this.data[M11] - this.data[M01] * this.data[M10];
        final double s1 = this.data[M00] * this.data[M12] - this.data[M02] * this.data[M10];
        final double s2 = this.data[M00] * this.data[M13] - this.data[M03] * this.data[M10];
        final double s3 = this.data[M01] * this.data[M12] - this.data[M02] * this.data[M13];
        final double s4 = this.data[M01] * this.data[M13] - this.data[M03] * this.data[M11];
        final double s5 = this.data[M02] * this.data[M13] - this.data[M03] * this.data[M12];

        final double c5 = this.data[M22] * this.data[M33] - this.data[M23] * this.data[M32];
        final double c4 = this.data[M21] * this.data[M33] - this.data[M23] * this.data[M31];
        final double c3 = this.data[M21] * this.data[M32] - this.data[M22] * this.data[M31];
        final double c2 = this.data[M20] * this.data[M33] - this.data[M23] * this.data[M30];
        final double c1 = this.data[M20] * this.data[M32] - this.data[M22] * this.data[M30];
        final double c0 = this.data[M20] * this.data[M31] - this.data[M21] * this.data[M30];

        return s0 * c5 - s1 * c4 + s2 * c3 + s3 * c2 - s4 * c1 + s5 * c0;
    }

    public Matrix4x4d adjugate()
    {
        final double s0 = this.data[M00] * this.data[M11] - this.data[M01] * this.data[M10];
        final double s1 = this.data[M00] * this.data[M12] - this.data[M02] * this.data[M10];
        final double s2 = this.data[M00] * this.data[M13] - this.data[M03] * this.data[M10];
        final double s3 = this.data[M01] * this.data[M12] - this.data[M02] * this.data[M13];
        final double s4 = this.data[M01] * this.data[M13] - this.data[M03] * this.data[M11];
        final double s5 = this.data[M02] * this.data[M13] - this.data[M03] * this.data[M12];

        final double c5 = this.data[M22] * this.data[M33] - this.data[M23] * this.data[M32];
        final double c4 = this.data[M21] * this.data[M33] - this.data[M23] * this.data[M31];
        final double c3 = this.data[M21] * this.data[M32] - this.data[M22] * this.data[M31];
        final double c2 = this.data[M20] * this.data[M33] - this.data[M23] * this.data[M30];
        final double c1 = this.data[M20] * this.data[M32] - this.data[M22] * this.data[M30];
        final double c0 = this.data[M20] * this.data[M31] - this.data[M21] * this.data[M30];

        final Matrix4x4d mat = new Matrix4x4d();

        mat.data[M00] = this.data[M11] * c5 - this.data[M12] * c4 + this.data[M13] * c3;
        mat.data[M01] = -this.data[M01] * c5 + this.data[M02] * c4 - this.data[M03] * c3;
        mat.data[M02] = this.data[M31] * s5 - this.data[M32] * s4 + this.data[M33] * s3;
        mat.data[M03] = -this.data[M21] * s5 + this.data[M22] * s4 - this.data[M23] * s3;

        mat.data[M10] = -this.data[M10] * c5 + this.data[M12] * c2 - this.data[M13] * c1;
        mat.data[M11] = this.data[M00] * c5 - this.data[M02] * c2 + this.data[M03] * c1;
        mat.data[M12] = -this.data[M30] * s5 + this.data[M32] * s2 - this.data[M33] * s1;
        mat.data[M13] = this.data[M20] * s5 - this.data[M22] * s2 + this.data[M23] * s1;

        mat.data[M20] = this.data[M10] * c4 - this.data[M11] * c2 + this.data[M13] * c0;
        mat.data[M21] = -this.data[M00] * c4 + this.data[M01] * c2 - this.data[M03] * c0;
        mat.data[M22] = this.data[M30] * s4 - this.data[M31] * s2 + this.data[M33] * s0;
        mat.data[M23] = -this.data[M20] * s4 + this.data[M21] * s2 - this.data[M23] * s0;

        mat.data[M30] = -this.data[M10] * c3 + this.data[M11] * c1 - this.data[M12] * c0;
        mat.data[M31] = this.data[M00] * c3 - this.data[M01] * c1 + this.data[M02] * c0;
        mat.data[M32] = -this.data[M30] * s3 + this.data[M31] * s1 - this.data[M32] * s0;
        mat.data[M33] = this.data[M20] * s3 - this.data[M21] * s1 + this.data[M22] * s0;

        return mat;
    }

    public Matrix4x4d inverse()
    {
        final double s0 = this.data[M00] * this.data[M11] - this.data[M01] * this.data[M10];
        final double s1 = this.data[M00] * this.data[M12] - this.data[M02] * this.data[M10];
        final double s2 = this.data[M00] * this.data[M13] - this.data[M03] * this.data[M10];
        final double s3 = this.data[M01] * this.data[M12] - this.data[M02] * this.data[M13];
        final double s4 = this.data[M01] * this.data[M13] - this.data[M03] * this.data[M11];
        final double s5 = this.data[M02] * this.data[M13] - this.data[M03] * this.data[M12];

        final double c5 = this.data[M22] * this.data[M33] - this.data[M23] * this.data[M32];
        final double c4 = this.data[M21] * this.data[M33] - this.data[M23] * this.data[M31];
        final double c3 = this.data[M21] * this.data[M32] - this.data[M22] * this.data[M31];
        final double c2 = this.data[M20] * this.data[M33] - this.data[M23] * this.data[M30];
        final double c1 = this.data[M20] * this.data[M32] - this.data[M22] * this.data[M30];
        final double c0 = this.data[M20] * this.data[M31] - this.data[M21] * this.data[M30];

        final Matrix4x4d mat = new Matrix4x4d();
        final double rcpdet = 1.0 / (s0 * c5 - s1 * c4 + s2 * c3 + s3 * c2 - s4 * c1 + s5 * c0);

        mat.data[M00] = (this.data[M11] * c5 - this.data[M12] * c4 + this.data[M13] * c3) * rcpdet;
        mat.data[M01] = (-this.data[M01] * c5 + this.data[M02] * c4 - this.data[M03] * c3) * rcpdet;
        mat.data[M02] = (this.data[M31] * s5 - this.data[M32] * s4 + this.data[M33] * s3) * rcpdet;
        mat.data[M03] = (-this.data[M21] * s5 + this.data[M22] * s4 - this.data[M23] * s3) * rcpdet;

        mat.data[M10] = (-this.data[M10] * c5 + this.data[M12] * c2 - this.data[M13] * c1) * rcpdet;
        mat.data[M11] = (this.data[M00] * c5 - this.data[M02] * c2 + this.data[M03] * c1) * rcpdet;
        mat.data[M12] = (-this.data[M30] * s5 + this.data[M32] * s2 - this.data[M33] * s1) * rcpdet;
        mat.data[M13] = (this.data[M20] * s5 - this.data[M22] * s2 + this.data[M23] * s1) * rcpdet;

        mat.data[M20] = (this.data[M10] * c4 - this.data[M11] * c2 + this.data[M13] * c0) * rcpdet;
        mat.data[M21] = (-this.data[M00] * c4 + this.data[M01] * c2 - this.data[M03] * c0) * rcpdet;
        mat.data[M22] = (this.data[M30] * s4 - this.data[M31] * s2 + this.data[M33] * s0) * rcpdet;
        mat.data[M23] = (-this.data[M20] * s4 + this.data[M21] * s2 - this.data[M23] * s0) * rcpdet;

        mat.data[M31] = (this.data[M00] * c3 - this.data[M01] * c1 + this.data[M02] * c0) * rcpdet;
        mat.data[M30] = (-this.data[M10] * c3 + this.data[M11] * c1 - this.data[M12] * c0) * rcpdet;
        mat.data[M33] = (this.data[M20] * s3 - this.data[M21] * s1 + this.data[M22] * s0) * rcpdet;
        mat.data[M32] = (-this.data[M30] * s3 + this.data[M31] * s1 - this.data[M32] * s0) * rcpdet;

        return mat;
    }

    public Matrix4x4d transpose()
    {
        final Matrix4x4d mat = new Matrix4x4d();
        for (int y = 0; y < 4; y++)
        {
            for (int x = 0; x < 4; x++)
            {
                mat.data[y * 4 + x] = this.data[x * 4 + y];
            }
        }
        return mat;
    }

    public Matrix4x4d multiply(final Matrix4x4d mat)
    {
        final Matrix4x4d r = new Matrix4x4d();

        r.data[M00] = this.data[M00] * mat.data[M00] + this.data[M01] * mat.data[M10] + this.data[M02] * mat.data[M20] + this.data[M03] * mat.data[M30];
        r.data[M01] = this.data[M00] * mat.data[M01] + this.data[M01] * mat.data[M11] + this.data[M02] * mat.data[M21] + this.data[M03] * mat.data[M31];
        r.data[M02] = this.data[M00] * mat.data[M02] + this.data[M01] * mat.data[M12] + this.data[M02] * mat.data[M22] + this.data[M03] * mat.data[M32];
        r.data[M03] = this.data[M00] * mat.data[M03] + this.data[M01] * mat.data[M13] + this.data[M02] * mat.data[M23] + this.data[M03] * mat.data[M33];

        r.data[M10] = this.data[M10] * mat.data[M00] + this.data[M11] * mat.data[M10] + this.data[M12] * mat.data[M20] + this.data[M13] * mat.data[M30];
        r.data[M11] = this.data[M10] * mat.data[M01] + this.data[M11] * mat.data[M11] + this.data[M12] * mat.data[M21] + this.data[M13] * mat.data[M31];
        r.data[M12] = this.data[M10] * mat.data[M02] + this.data[M11] * mat.data[M12] + this.data[M12] * mat.data[M22] + this.data[M13] * mat.data[M32];
        r.data[M13] = this.data[M10] * mat.data[M03] + this.data[M11] * mat.data[M13] + this.data[M12] * mat.data[M23] + this.data[M13] * mat.data[M33];

        r.data[M20] = this.data[M20] * mat.data[M00] + this.data[M21] * mat.data[M10] + this.data[M22] * mat.data[M20] + this.data[M23] * mat.data[M30];
        r.data[M21] = this.data[M20] * mat.data[M01] + this.data[M21] * mat.data[M11] + this.data[M22] * mat.data[M21] + this.data[M23] * mat.data[M31];
        r.data[M22] = this.data[M20] * mat.data[M02] + this.data[M21] * mat.data[M12] + this.data[M22] * mat.data[M22] + this.data[M23] * mat.data[M32];
        r.data[M23] = this.data[M20] * mat.data[M03] + this.data[M21] * mat.data[M13] + this.data[M22] * mat.data[M23] + this.data[M23] * mat.data[M33];

        r.data[M30] = this.data[M30] * mat.data[M00] + this.data[M31] * mat.data[M10] + this.data[M32] * mat.data[M20] + this.data[M33] * mat.data[M30];
        r.data[M31] = this.data[M30] * mat.data[M01] + this.data[M31] * mat.data[M11] + this.data[M32] * mat.data[M21] + this.data[M33] * mat.data[M31];
        r.data[M32] = this.data[M30] * mat.data[M02] + this.data[M31] * mat.data[M12] + this.data[M32] * mat.data[M22] + this.data[M33] * mat.data[M32];
        r.data[M33] = this.data[M30] * mat.data[M03] + this.data[M31] * mat.data[M13] + this.data[M32] * mat.data[M23] + this.data[M33] * mat.data[M33];

        return r;
    }

    public Matrix4x4d toMatrix3x3()
    {
        final Matrix4x4d ret = new Matrix4x4d();

        for (int y = 0; y < 4; y++)
        {
            for (int x = 0; x < 4; x++)
            {
                if (x < 3 && y < 3)
                {
                    ret.data[x + y * 4] = this.data[x + y * 4];
                }
                else
                {
                    if (x == 3 && y == 3)
                    {
                        ret.data[x + y * 4] = 1;
                    }
                    else
                    {
                        ret.data[x + y * 4] = 0;
                    }
                }
            }
        }

        return ret;
    }

    public Vector3d multiply(final Vector3d vec)
    {
        final double x = vec.x;
        final double y = vec.y;
        final double z = vec.z;
        final double rcpw = 1.0f / (x * this.data[M30] + y * this.data[M31] + z * this.data[M32] + this.data[M33]);
        vec.x = (x * this.data[M00] + y * this.data[M01] + z * this.data[M02] + this.data[M03]) * rcpw;
        vec.y = (x * this.data[M10] + y * this.data[M11] + z * this.data[M12] + this.data[M13]) * rcpw;
        vec.z = (x * this.data[M20] + y * this.data[M21] + z * this.data[M22] + this.data[M23]) * rcpw;
        return vec;
    }

    public Vector3d multiply3x3(final Vector3d vec)
    {
        final double x = vec.x;
        final double y = vec.y;
        final double z = vec.z;
        vec.x = x * this.data[M00] + y * this.data[M01] + z * this.data[M02];
        vec.y = x * this.data[M10] + y * this.data[M11] + z * this.data[M12];
        vec.z = x * this.data[M20] + y * this.data[M21] + z * this.data[M22];
        return vec;
    }

    public Vector4d multiply(final Vector4d vec)
    {
        final double x = vec.x;
        final double y = vec.y;
        final double z = vec.z;
        final double w = vec.w;
        vec.x = x * this.data[M00] + y * this.data[M01] + z * this.data[M02] + w * this.data[M03];
        vec.y = x * this.data[M10] + y * this.data[M11] + z * this.data[M12] + w * this.data[M13];
        vec.z = x * this.data[M20] + y * this.data[M21] + z * this.data[M22] + w * this.data[M23];
        vec.w = x * this.data[M30] + y * this.data[M31] + z * this.data[M32] + w * this.data[M33];
        return vec;
    }

    public void writeInto(final double[] array, final int offset)
    {
        System.arraycopy(this.data, 0, array, offset, 16);
    }

    @Override
    public String toString()
    {
        return "{" + this.data[M00] + ", " + this.data[M01] + ", " + this.data[M02] + ", " + this.data[M03] + "\n" + this.data[M10] + ", " + this.data[M11]
                + ", " + this.data[M12] + ", " + this.data[M13] + "\n" + this.data[M20] + ", " + this.data[M21] + ", " + this.data[M22] + ", " + this.data[M23]
                + "\n" + this.data[M30] + ", " + this.data[M31] + ", " + this.data[M32] + ", " + this.data[M33] + "}";
    }
}
