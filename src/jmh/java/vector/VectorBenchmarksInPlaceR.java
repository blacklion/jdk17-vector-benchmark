/*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\
!! THIS FILE IS GENERATED WITH genBenchmarks.pl SCRIPT. DO NOT EDIT! !!
\!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
/*****************************************************************************
 * Copyright (c) 2022, Lev Serebryakov <lev@serebryakov.spb.ru>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ****************************************************************************/

package vector;

import vectorapi.VO;
import vectorapi.VOVec;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;

@Fork(2)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 10, time = 5)
@Threads(1)
@State(org.openjdk.jmh.annotations.Scope.Thread)
public class VectorBenchmarksInPlaceR {
    private final static int SEED = 42; // Carefully selected, plucked by hands random number

    private final static int DATA_SIZE = 65536;

    @Param({"3", "4", "7", "8", "15", "128", "1024", "65536"})
    public int callSize;

    private final static int MAX_OFFSET = 512 / 32 - 1; // Max vector size in `float`s;
    @Param({"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"})
    public int startOffset;

    private float rvx[];
    private float rvy[];
    private float rvz[];
    private float rvd[];

    private float cvx[];
    private float cvy[];
    private float cvz[];
    private float cvd[];

    private float rsx;
    private float rsy;
    private float rsz;

    private float csx[];
    private float csy[];
    private float csz[];


    @Setup(Level.Trial)
    public void Setup() {
        Random r = new Random(SEED);

        rvx = new float[DATA_SIZE + MAX_OFFSET];
        rvy = new float[DATA_SIZE + MAX_OFFSET];
        rvz = new float[DATA_SIZE + MAX_OFFSET];
        rvd = new float[DATA_SIZE + MAX_OFFSET];
        for (int i = 0; i < rvx.length; i++) {
            rvx[i] = r.nextFloat() * 2.0f - 1.0f;
            rvy[i] = r.nextFloat() * 2.0f - 1.0f;
            rvd[i] = rvz[i] = r.nextFloat() * 2.0f - 1.0f;
        }

        cvx = new float[(DATA_SIZE + MAX_OFFSET) * 2];
        cvy = new float[(DATA_SIZE + MAX_OFFSET) * 2];
        cvz = new float[(DATA_SIZE + MAX_OFFSET) * 2];
        cvd = new float[(DATA_SIZE + MAX_OFFSET) * 2];
        for (int i = 0; i < cvx.length; i++) {
            cvx[i] = r.nextFloat() * 2.0f - 1.0f;
            cvy[i] = r.nextFloat() * 2.0f - 1.0f;
            cvd[i] = cvz[i] = r.nextFloat() * 2.0f - 1.0f;
        }

        rsx = r.nextFloat() * 2.0f - 1.0f;
        rsy = r.nextFloat() * 2.0f - 1.0f;
        rsz = r.nextFloat() * 2.0f - 1.0f;

        csx = new float[] { r.nextFloat() * 2.0f - 1.0f, r.nextFloat() * 2.0f - 1.0f };
        csy = new float[] { r.nextFloat() * 2.0f - 1.0f, r.nextFloat() * 2.0f - 1.0f };
        csz = new float[] { r.nextFloat() * 2.0f - 1.0f, r.nextFloat() * 2.0f - 1.0f };
    }

    @Setup(Level.Invocation)
    public void SetupInPlaceData() {
        System.arraycopy(rvd, 0, rvz, 0, rvd.length);
    }


    @Benchmark
    public void VO_rv_10log10_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_10log10_i(rvz, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_10log10_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_10log10_i(rvz, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_10log10_rs_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_10log10_rs_i(rvz, i, rsx, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_10log10_rs_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_10log10_rs_i(rvz, i, rsx, callSize);
        }
    }

    @Benchmark
    public void VO_rv_abs_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_abs_i(rvz, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_abs_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_abs_i(rvz, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_add_rs_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_add_rs_i(rvz, i, rsx, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_add_rs_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_add_rs_i(rvz, i, rsx, callSize);
        }
    }

    @Benchmark
    public void VO_rv_add_rv_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_add_rv_i(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_add_rv_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_add_rv_i(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_div_rs_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_div_rs_i(rvz, i, rsx, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_div_rs_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_div_rs_i(rvz, i, rsx, callSize);
        }
    }

    @Benchmark
    public void VO_rv_div_rv_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_div_rv_i(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_div_rv_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_div_rv_i(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_exp_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_exp_i(rvz, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_exp_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_exp_i(rvz, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_max_rv_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_max_rv_i(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_max_rv_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_max_rv_i(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_mul_rs_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_mul_rs_i(rvz, i, rsx, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_mul_rs_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_mul_rs_i(rvz, i, rsx, callSize);
        }
    }

    @Benchmark
    public void VO_rv_mul_rv_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_mul_rv_i(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_mul_rv_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_mul_rv_i(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_rs_lin_rv_rs_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_rs_lin_rv_rs_i(rvz, i, rsz, rvx, i, rsx, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_rs_lin_rv_rs_i() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_rs_lin_rv_rs_i(rvz, i, rsz, rvx, i, rsx, callSize);
        }
    }
}