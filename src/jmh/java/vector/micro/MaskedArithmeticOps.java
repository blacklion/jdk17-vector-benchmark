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

import static jdk.incubator.vector.VectorOperators.ATAN2;
import static jdk.incubator.vector.VectorOperators.COS;
import static jdk.incubator.vector.VectorOperators.HYPOT;
import static jdk.incubator.vector.VectorOperators.SIN;

/*
 * This benchmark tries to determine the cost of masked lanewise operations
 * typical for some complex number operations..
 */
@Fork(2)
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 10, time = 5)
@Threads(1)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MaskedArithmeticOps {
	private final static int SEED = 42; // Carefully selected, plucked by hands random number

	private final static VectorSpecies<Float> PFS = FloatVector.SPECIES_PREFERRED;
	private final static int EPV = PFS.length();

	private final static VectorMask<Float> MASK_C_EVEN;
	private final static VectorMask<Float> MASK_C_ODD;

	static {
		boolean[] even = new boolean[EPV];
		for (int i = 0; i < even.length; i++)
			even[i] = i %2 == 0;
		MASK_C_EVEN = VectorMask.fromArray(PFS, even, 0);
		MASK_C_ODD = MASK_C_EVEN.not();
	}

	private FloatVector vx;
	private FloatVector vy;

	@Setup(Level.Trial)
	public void Setup() {
		Random r = new Random(SEED);

		float[] x = new float[EPV];
		float[] y = new float[EPV];

        for (int i = 0; i < x.length; i++) {
			x[i] = r.nextFloat() * 2.0f - 1.0f;
			y[i] = r.nextFloat() * 2.0f - 1.0f;
		}
		vx = FloatVector.fromArray(PFS, x, 0);
		vy = FloatVector.fromArray(PFS, y, 0);
	}

	@Benchmark
	public void addEvenSubOddWithMasksAndBlend(Blackhole bh) {
		bh.consume(vx.add(vy, MASK_C_EVEN).blend(vx.sub(vy, MASK_C_ODD), MASK_C_ODD));
	}

	@Benchmark
	public void addEvenSubOddAndBlend(Blackhole bh) {
		bh.consume(vx.add(vy).blend(vx.sub(vy), MASK_C_ODD));
	}

	@Benchmark
	public void cosEvenSinOddWithMasksAndBlend(Blackhole bh) {
		bh.consume(vx.lanewise(COS, MASK_C_EVEN).blend(vx.lanewise(SIN, MASK_C_ODD), MASK_C_ODD));
	}

	@Benchmark
	public void cosEvenSinOddAndBlend(Blackhole bh) {
		bh.consume(vx.lanewise(COS).blend(vx.lanewise(SIN), MASK_C_ODD));
	}

	@Benchmark
	public void hypotEvenAtan2OddWithMasksAndBlend(Blackhole bh) {
		bh.consume(vx.lanewise(HYPOT, vy, MASK_C_EVEN).blend(vx.lanewise(ATAN2, vy, MASK_C_ODD), MASK_C_ODD));
	}

	@Benchmark
	public void hypotEvenAtan2OddAndBlend(Blackhole bh) {
		bh.consume(vx.lanewise(HYPOT, vy).blend(vx.lanewise(ATAN2, vy), MASK_C_ODD));
	}
}
