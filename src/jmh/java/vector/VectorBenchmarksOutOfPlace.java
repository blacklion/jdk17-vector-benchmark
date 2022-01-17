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
public class VectorBenchmarksOutOfPlace {
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


    @Benchmark
    public void VO_cs_div_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cs_div_cv(cvz, i, csx, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cs_div_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cs_div_cv(cvz, i, csx, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_10log10() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_10log10(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_10log10() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_10log10(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_10log10_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_10log10_rs(cvz, i, cvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_10log10_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_10log10_rs(cvz, i, cvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VO_cv_abs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_abs(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_abs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_abs(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_add_cs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_add_cs(cvz, i, cvx, i, csy, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_add_cs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_add_cs(cvz, i, cvx, i, csy, callSize);
        }
    }

    @Benchmark
    public void VO_cv_add_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_add_cv(cvz, i, cvx, i, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_add_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_add_cv(cvz, i, cvx, i, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_add_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_add_rs(cvz, i, cvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_add_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_add_rs(cvz, i, cvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VO_cv_add_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_add_rv(cvz, i, cvx, i, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_add_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_add_rv(cvz, i, cvx, i, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_arg() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_arg(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_arg() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_arg(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_argmul_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_argmul_rs(cvz, i, cvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_argmul_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_argmul_rs(cvz, i, cvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VO_cv_conj() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_conj(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_conj() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_conj(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_cpy() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_cpy(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_cpy() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_cpy(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_div_cs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_div_cs(cvz, i, cvx, i, csy, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_div_cs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_div_cs(cvz, i, cvx, i, csy, callSize);
        }
    }

    @Benchmark
    public void VO_cv_div_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_div_cv(cvz, i, cvx, i, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_div_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_div_cv(cvz, i, cvx, i, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_div_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_div_rs(cvz, i, cvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_div_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_div_rs(cvz, i, cvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VO_cv_div_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_div_rv(cvz, i, cvx, i, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_div_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_div_rv(cvz, i, cvx, i, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_dot_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_dot_cv(csz, cvx, i, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_dot_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_dot_cv(csz, cvx, i, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_exp() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_exp(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_exp() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_exp(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_im() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_im(rvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_im() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_im(rvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_max() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_max(csz, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_max() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_max(csz, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_max_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_max_cv(cvz, i, cvx, i, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_max_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_max_cv(cvz, i, cvx, i, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_maxarg(Blackhole bh) {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            bh.consume(VO.cv_maxarg(cvx, i, callSize));
        }
    }

    @Benchmark
    public void VOVec_cv_maxarg(Blackhole bh) {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            bh.consume(VOVec.cv_maxarg(cvx, i, callSize));
        }
    }

    @Benchmark
    public void VO_cv_mul_cs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_mul_cs(cvz, i, cvx, i, csy, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_mul_cs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_mul_cs(cvz, i, cvx, i, csy, callSize);
        }
    }

    @Benchmark
    public void VO_cv_mul_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_mul_cv(cvz, i, cvx, i, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_mul_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_mul_cv(cvz, i, cvx, i, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_mul_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_mul_rs(cvz, i, cvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_mul_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_mul_rs(cvz, i, cvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VO_cv_mul_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_mul_rv(cvz, i, cvx, i, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_mul_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_mul_rv(cvz, i, cvx, i, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_p2r() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_p2r(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_p2r() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_p2r(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_r2p() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_r2p(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_r2p() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_r2p(cvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_re() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_re(rvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_re() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_re(rvz, i, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_cv_rs_lin_rv_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_rs_lin_rv_rs(cvz, i, cvx, i, rsx, rvy, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_rs_lin_rv_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_rs_lin_rv_rs(cvz, i, cvx, i, rsx, rvy, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VO_cv_sum() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.cv_sum(csz, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_cv_sum() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.cv_sum(csz, cvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_rs_div_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rs_div_cv(cvz, i, rsx, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rs_div_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rs_div_cv(cvz, i, rsx, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_rs_div_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rs_div_rv(rvz, i, rsx, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rs_div_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rs_div_rv(rvz, i, rsx, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_10log10() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_10log10(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_10log10() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_10log10(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_10log10_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_10log10_rs(rvz, i, rvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_10log10_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_10log10_rs(rvz, i, rvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VO_rv_abs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_abs(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_abs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_abs(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_add_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_add_rs(rvz, i, rvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_add_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_add_rs(rvz, i, rvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VO_rv_add_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_add_rv(rvz, i, rvx, i, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_add_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_add_rv(rvz, i, rvx, i, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_cpy() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_cpy(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_cpy() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_cpy(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_cs_lin_rv_cs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_cs_lin_rv_cs(cvz, i, rvx, i, csx, rvy, i, csy, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_cs_lin_rv_cs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_cs_lin_rv_cs(cvz, i, rvx, i, csx, rvy, i, csy, callSize);
        }
    }

    @Benchmark
    public void VO_rv_cvt() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_cvt(cvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_cvt() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_cvt(cvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_div_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_div_cv(cvz, i, rvx, i, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_div_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_div_cv(cvz, i, rvx, i, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_div_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_div_rs(rvz, i, rvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_div_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_div_rs(rvz, i, rvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VO_rv_div_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_div_rv(rvz, i, rvx, i, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_div_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_div_rv(rvz, i, rvx, i, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_dot_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_dot_cv(csz, rvx, i, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_dot_cv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_dot_cv(csz, rvx, i, cvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_dot_rv(Blackhole bh) {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            bh.consume(VO.rv_dot_rv(rvx, i, rvy, i, callSize));
        }
    }

    @Benchmark
    public void VOVec_rv_dot_rv(Blackhole bh) {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            bh.consume(VOVec.rv_dot_rv(rvx, i, rvy, i, callSize));
        }
    }

    @Benchmark
    public void VO_rv_exp() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_exp(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_exp() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_exp(rvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_expi() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_expi(cvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_expi() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_expi(cvz, i, rvx, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_max(Blackhole bh) {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            bh.consume(VO.rv_max(rvx, i, callSize));
        }
    }

    @Benchmark
    public void VOVec_rv_max(Blackhole bh) {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            bh.consume(VOVec.rv_max(rvx, i, callSize));
        }
    }

    @Benchmark
    public void VO_rv_max_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_max_rv(rvz, i, rvx, i, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_max_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_max_rv(rvz, i, rvx, i, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_maxarg(Blackhole bh) {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            bh.consume(VO.rv_maxarg(rvx, i, callSize));
        }
    }

    @Benchmark
    public void VOVec_rv_maxarg(Blackhole bh) {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            bh.consume(VOVec.rv_maxarg(rvx, i, callSize));
        }
    }

    @Benchmark
    public void VO_rv_mul_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_mul_rs(rvz, i, rvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_mul_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_mul_rs(rvz, i, rvx, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VO_rv_mul_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_mul_rv(rvz, i, rvx, i, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_mul_rv() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_mul_rv(rvz, i, rvx, i, rvy, i, callSize);
        }
    }

    @Benchmark
    public void VO_rv_rs_lin_rv_cs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_rs_lin_rv_cs(cvz, i, rvx, i, rsx, rvy, i, csy, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_rs_lin_rv_cs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_rs_lin_rv_cs(cvz, i, rvx, i, rsx, rvy, i, csy, callSize);
        }
    }

    @Benchmark
    public void VO_rv_rs_lin_rv_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VO.rv_rs_lin_rv_rs(rvz, i, rvx, i, rsx, rvy, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VOVec_rv_rs_lin_rv_rs() {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            VOVec.rv_rs_lin_rv_rs(rvz, i, rvx, i, rsx, rvy, i, rsy, callSize);
        }
    }

    @Benchmark
    public void VO_rv_sum(Blackhole bh) {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            bh.consume(VO.rv_sum(rvx, i, callSize));
        }
    }

    @Benchmark
    public void VOVec_rv_sum(Blackhole bh) {
        for (int i = startOffset; i <= DATA_SIZE + startOffset - callSize; i += callSize) {
            bh.consume(VOVec.rv_sum(rvx, i, callSize));
        }
    }
}