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
import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorSpecies;
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

/*
 * This benchmark tries to determine the best way to load real numbers
 * to vector of complex numbers as real parts, setting imaginary
 * parts to zero.
 *
 * Such operation must consume SPECIES_PREFERRED.length()/2 real
 * numbers and produce two one SPECIES_PREFERRED vectors.
 */
@Fork(2)
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 10, time = 5)
@Threads(1)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class LoadRealNumbersToComplexVectorReOnly {
	private final static int SEED = 42; // Carefully selected, plucked by hands random number

	private final static VectorSpecies<Float> PFS = FloatVector.SPECIES_PREFERRED;
	private final static int EPV = PFS.length();
	private final static VectorSpecies<Float> PFS2 = VectorSpecies.of(Float.TYPE, VectorShape.forBitSize(PFS.vectorBitSize() / 2));

	private final static VectorMask<Float> MASK_C_EVEN;
	private final static VectorMask<Float> MASK_C_ODD;

	private final static VectorShuffle<Float> SHUFFLE_RV_TO_CV_RE;
	private final static VectorShuffle<Float> SHUFFLE_RV_TO_CV_RE_ZERO;
	private final static int[] LOAD_RV_TO_CV_RE;

	private final static FloatVector ZERO = FloatVector.zero(PFS);

	static {
		boolean[] even = new boolean[EPV];
		for (int i = 0; i < even.length; i++)
			even[i] = i % 2 == 0;

		MASK_C_EVEN = VectorMask.fromArray(PFS, even, 0);
		MASK_C_ODD = MASK_C_EVEN.not();

		SHUFFLE_RV_TO_CV_RE = VectorShuffle.fromOp(PFS, i -> i / 2);
		SHUFFLE_RV_TO_CV_RE_ZERO = VectorShuffle.fromOp(PFS, i -> (i % 2 == 0) ? (i / 2) : (EPV - 1));

		LOAD_RV_TO_CV_RE = SHUFFLE_RV_TO_CV_RE.toArray();
	}

	private float[] x;

	@Setup(Level.Trial)
	public void Setup() {
		Random r = new Random(SEED);

		x = new float[EPV];
		for (int i = 0; i < x.length; i++)
			x[i] = r.nextFloat() * 2.0f - 1.0f;
	}

	@Benchmark
	public void loadWithIndexMapAndMask(Blackhole bh) {
		bh.consume(FloatVector.fromArray(PFS, x, 0, LOAD_RV_TO_CV_RE, 0, MASK_C_EVEN));
	}

	@Benchmark
	public void loadHalfReshapeRearrangeAndBlend(Blackhole bh) {
		bh.consume(FloatVector.fromArray(PFS2, x, 0)
            .reinterpretShape(PFS, 0).reinterpretAsFloats()
            .rearrange(SHUFFLE_RV_TO_CV_RE)
            .blend(ZERO, MASK_C_ODD)
        );
	}

	@Benchmark
	public void loadHalfReshapeRearrange(Blackhole bh) {
		bh.consume(FloatVector.fromArray(PFS2, x, 0)
            .reinterpretShape(PFS, 0).reinterpretAsFloats()
            .rearrange(SHUFFLE_RV_TO_CV_RE_ZERO)
        );
	}
}
