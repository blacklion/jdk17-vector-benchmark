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

package vector.micro;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorSpecies;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
 * This benchmark tries to determine the best way to load complex numbers
 * to two different arrays, one with densely packed real parts and other
 * one with densely packed imaginary parts.
 *
 * Such operation must consume SPECIES_PREFERRED.length() complex
 * numbers (twice more array elements) and produce two
 * SPECIES_PREFERRED vectors.
 */
@Fork(2)
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 10, time = 5)
@Threads(1)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class SplitComplexVectorToReAndImVectors {
	private final static int SEED = 42; // Carefully selected, plucked by hands random number

	private final static VectorSpecies<Float> PFS = FloatVector.SPECIES_PREFERRED;
	private final static int EPV = PFS.length();
	private final static int EPV2 = PFS.length() / 2;

	private final static VectorShuffle<Float> SHUFFLE_CV_TO_CV_PACK_RE_FIRST;
	private final static VectorShuffle<Float> SHUFFLE_CV_TO_CV_PACK_IM_FIRST;
	private final static VectorShuffle<Float> SHUFFLE_CV_TO_CV_PACK_RE_SECOND;
	private final static VectorShuffle<Float> SHUFFLE_CV_TO_CV_PACK_IM_SECOND;

	private final static int[] LOAD_CV_TO_CV_PACK_RE;
	private final static int[] LOAD_CV_TO_CV_PACK_IM;

	private final static VectorMask<Float> MASK_SECOND_HALF;

	static {
		boolean[] sh = new boolean[EPV];
		Arrays.fill(sh, EPV / 2, sh.length, true);
		MASK_SECOND_HALF = VectorMask.fromArray(PFS, sh, 0);

		// [(re0, im0), (re1, im1), ...] -> [re0, re1, ..., re_len, ?, ...]
		SHUFFLE_CV_TO_CV_PACK_RE_FIRST = VectorShuffle.fromOp(PFS, i -> (i < EPV2) ? i * 2 : 0);
		// [(re0, im0), (re1, im1), ...] -> [im0, im1, ..., im_len, ?, ...]
		SHUFFLE_CV_TO_CV_PACK_IM_FIRST = VectorShuffle.fromOp(PFS, i -> (i < EPV2) ? i * 2 + 1 : 0);
		// [(re0, im0), (re1, im1), ...] -> [?, ..., re0, re1, ..., re_len]
		SHUFFLE_CV_TO_CV_PACK_RE_SECOND = VectorShuffle.fromOp(PFS, i -> (i >= EPV2) ? i * 2 - EPV : 0);
		// [(re0, im0), (re1, im1), ...] -> [?, ..., im0, im1, ..., im_len]
		SHUFFLE_CV_TO_CV_PACK_IM_SECOND = VectorShuffle.fromOp(PFS, i -> (i >= EPV2) ? i * 2 - EPV + 1 : 0);

		LOAD_CV_TO_CV_PACK_RE = new int[EPV];
		LOAD_CV_TO_CV_PACK_IM = new int[EPV];
		for (int i = 0; i < EPV; i++) {
			LOAD_CV_TO_CV_PACK_RE[i] = i * 2;
			LOAD_CV_TO_CV_PACK_IM[i] = i * 2 + 1;
		}
	}

	private float[] x;

	@Setup(Level.Trial)
	public void Setup() {
		Random r = new Random(SEED);
		x = new float[EPV * 2];
		for (int i = 0; i < x.length; i++)
			x[i] = r.nextFloat() * 2.0f - 1.0f;
	}

	@Benchmark
	public void twoLoadsWithIndexMap(Blackhole bh) {
		final FloatVector vxre = FloatVector.fromArray(PFS, x, 0, LOAD_CV_TO_CV_PACK_RE, 0);
		final FloatVector vxim = FloatVector.fromArray(PFS, x, 0, LOAD_CV_TO_CV_PACK_IM, 0);
		bh.consume(vxre);
		bh.consume(vxim);
	}

	@Benchmark
	public void twoLoadsAndFourRearrangesAndTwoBlends(Blackhole bh) {
		final FloatVector vx1 = FloatVector.fromArray(PFS, x, 0);
		final FloatVector vx2 = FloatVector.fromArray(PFS, x, EPV);

		final FloatVector vx1re = vx1.rearrange(SHUFFLE_CV_TO_CV_PACK_RE_FIRST);
		final FloatVector vx1im = vx1.rearrange(SHUFFLE_CV_TO_CV_PACK_IM_FIRST);

		final FloatVector vx2re = vx2.rearrange(SHUFFLE_CV_TO_CV_PACK_RE_SECOND);
		final FloatVector vx2im = vx2.rearrange(SHUFFLE_CV_TO_CV_PACK_IM_SECOND);

		final FloatVector vxre = vx1re.blend(vx2re, MASK_SECOND_HALF);
		final FloatVector vxim = vx1im.blend(vx2im, MASK_SECOND_HALF);
		bh.consume(vxre);
		bh.consume(vxim);
	}
}
