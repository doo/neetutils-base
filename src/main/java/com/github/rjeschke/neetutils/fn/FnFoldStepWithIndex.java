package com.github.rjeschke.neetutils.fn;

/**
 * Created by a.koch on 31/07/14.
 */
public interface FnFoldStepWithIndex<Input, Result>
{
    public Result applyFoldStep(Input in, Result result, int index);
}