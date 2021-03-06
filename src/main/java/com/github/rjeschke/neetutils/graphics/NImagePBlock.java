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
package com.github.rjeschke.neetutils.graphics;

import java.util.Arrays;

/**
 *
 * @author René Jeschke (rene_jeschke@yahoo.de)
 *
 */
public class NImagePBlock
{
    public final NColor[] pixels;
    public final int      x;
    public final int      y;
    public final int      w;
    public final int      h;

    public NImagePBlock(final int x, final int y, final int w, final int h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.pixels = new NColor[w * h];
        Arrays.fill(this.pixels, NColor.BLACK_TRANS);
    }
}
