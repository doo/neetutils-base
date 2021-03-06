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

import java.nio.IntBuffer;

import com.github.rjeschke.neetutils.math.NMath;

/**
 *
 * @author René Jeschke (rene_jeschke@yahoo.de)
 *
 */
public class Vector2i
{
    public int x;
    public int y;

    public Vector2i()
    {
        // default
    }

    public Vector2i(final int x, final int y)
    {
        this.x = x;
        this.y = y;
    }

    public Vector2i(final int xy)
    {
        this.x = xy;
        this.y = xy;
    }

    public Vector2i(final Vector2i v)
    {
        this.x = v.x;
        this.y = v.y;
    }

    public static Vector2i of(final int x, final int y)
    {
        return new Vector2i(x, y);
    }

    public static Vector2i of(final Vector2i v)
    {
        return new Vector2i(v);
    }

    public Vector2i set(final int x, final int y)
    {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2i set(final int xy)
    {
        this.x = xy;
        this.y = xy;
        return this;
    }

    public Vector2i set(final Vector2i v)
    {
        this.x = v.x;
        this.y = v.y;
        return this;
    }

    public Vector2i set(final Vector3i v)
    {
        this.x = v.x;
        this.y = v.y;
        return this;
    }

    public Vector2i set(final Vector4i v)
    {
        this.x = v.x;
        this.y = v.y;
        return this;
    }

    public Vector2i setIdx(final int index, final int value)
    {
        if (index == 0)
        {
            this.x = value;
        }
        else
        {
            this.y = value;
        }
        return this;
    }

    public int get(final int index)
    {
        if (index == 0) return this.x;
        return this.y;
    }

    public Vector2i add(final Vector2i v)
    {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    public Vector2i sub(final Vector2i v)
    {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    public Vector2i mul(final Vector2i v)
    {
        this.x *= v.x;
        this.y *= v.y;
        return this;
    }

    public Vector2i div(final Vector2i v)
    {
        this.x /= v.x;
        this.y /= v.y;
        return this;
    }

    public Vector2i min(final Vector2i v)
    {
        this.x = Math.min(this.x, v.x);
        this.y = Math.min(this.y, v.y);
        return this;
    }

    public Vector2i max(final Vector2i v)
    {
        this.x = Math.max(this.x, v.x);
        this.y = Math.max(this.y, v.y);
        return this;
    }

    public Vector2i clamp(final Vector2i min, final Vector2i max)
    {
        this.x = NMath.clamp(this.x, min.x, max.x);
        this.y = NMath.clamp(this.y, min.y, max.y);
        return this;
    }

    public Vector2i swizzle(final int a, final int b)
    {
        return Vector2i.of(this.get(a), this.get(b));
    }

    public Vector3i swizzle(final int a, final int b, final int c)
    {
        return Vector3i.of(this.get(a), this.get(b), this.get(c));
    }

    public Vector4i swizzle(final int a, final int b, final int c, final int d)
    {
        return Vector4i.of(this.get(a), this.get(b), this.get(c), this.get(d));
    }

    public void into(final int[] arr, final int offset)
    {
        arr[offset] = this.x;
        arr[offset + 1] = this.y;
    }

    public void into(final IntBuffer buffer, final int offset)
    {
        buffer.put(offset, this.x);
        buffer.put(offset + 1, this.y);
    }

    @Override
    public Vector2i clone()
    {
        return new Vector2i(this.x, this.y);
    }

    @Override
    public String toString()
    {
        return "(" + this.x + ", " + this.y + ")";
    }
}
