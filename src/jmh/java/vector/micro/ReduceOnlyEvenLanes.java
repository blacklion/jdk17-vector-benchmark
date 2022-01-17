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

import jdk.incubator.vector.*;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static jdk.incubator.vector.VectorOperators.ADD;

/*
 * This benchmark tries to determine the best way to reduce some lanes
 * of SPECIES_PREFERRED vector with binary operation (addition in this case).
 *
 * This benchmark reduces even lines (0, 2, 4, ...) as it is typical
 * operation in complex numbers implementation.
 */
@Fork(2)
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 10, time = 5)
@Threads(1)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ReduceOnlyEvenLanes {
    private final static int SEED = 42; // Carefully selected, plucked by hands random number

	private final static VectorSpecies<Float> PFS = FloatVector.SPECIES_PREFERRED;
	private final static VectorSpecies<Float> PFS2 = VectorSpecies.of(Float.TYPE, VectorShape.forBitSize(PFS.vectorBitSize() / 2));
	private final static int EPV = PFS.length();
	private final static int EPV2 = EPV / 2;

	private final static VectorMask<Float> MASK_EVEN;
    private final static VectorMask<Float> MASK_ODD;
	private final static VectorMask<Float> MASK_FIRST_HALF;

	private final static VectorShuffle<Float> SHUFFLE_CV_TO_CV_FRONT_RE;

	static {
		boolean[] even = new boolean[EPV];
		boolean[] firstHalf = new boolean[EPV];
		for (int i = 0; i < even.length; i++) {
			even[i] = i % 2 == 0;
			firstHalf[i] = i < EPV2;
		}
		MASK_EVEN = VectorMask.fromArray(PFS, even, 0);
        MASK_ODD = MASK_EVEN.not();
		MASK_FIRST_HALF = VectorMask.fromArray(PFS, firstHalf, 0);

		// [(re0, im0), (re1, im1), ...] -> [re0, re1, ...]
		SHUFFLE_CV_TO_CV_FRONT_RE = VectorShuffle.fromOp(PFS, i -> i * 2 < EPV ? i * 2 : i);
	}

    private FloatVector vx;

    @Setup(Level.Trial)
    public void Setup() {
        Random r = new Random(SEED);

        float[] x = new float[EPV * 2];

        for (int i = 0; i < x.length; i++) {
            x[i] = r.nextFloat() * 2.0f - 1.0f;
        }
        vx = FloatVector.fromArray(PFS, x, 0);
    }

    @Benchmark
	public void reduceWithCombMask(Blackhole bh) {
		bh.consume(vx.reduceLanes(ADD, MASK_EVEN));
	}

    @Benchmark
	public void reshuffleReduceWithHalfMask(Blackhole bh) {
		bh.consume(vx.rearrange(SHUFFLE_CV_TO_CV_FRONT_RE).reduceLanes(ADD, MASK_FIRST_HALF));
	}

    @Benchmark
    public void reshuffleReshapeReduce(Blackhole bh) {
		bh.consume(vx.rearrange(SHUFFLE_CV_TO_CV_FRONT_RE).reinterpretShape(PFS2, 0).reinterpretAsFloats().reduceLanes(ADD));
	}

    @Benchmark
    public void blendWithZeroReduce(Blackhole bh) {
        bh.consume(vx.blend(0, MASK_ODD).reduceLanes(ADD));
    }
}
