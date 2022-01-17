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
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
 * This benchmark tries to determine the best way to add (broadcast) scalar
 * to even lines of a vector of preferable size.
 */
@Fork(2)
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 10, time = 5)
@Threads(1)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class AddScalarToVectorEvenLanes {
	private final static int SEED = 42; // Carefully selected, plucked by hands random number

	private final static VectorSpecies<Float> PFS = FloatVector.SPECIES_PREFERRED;
	private final static int EPV = PFS.length();
	private final static VectorMask<Float> MASK_C_EVEN;

	static {
		boolean[] even = new boolean[EPV];
		for (int i = 0; i < even.length; i++)
            even[i] = i % 2 == 0;
		MASK_C_EVEN = VectorMask.fromArray(PFS, even, 0);
	}

    private float y;
    private FloatVector vx;
	private FloatVector vy;

	@Setup(Level.Trial)
	public void Setup() {
		Random r = new Random(SEED);

        float[] x = new float[EPV];
		for (int i = 0; i < x.length; i++)
			x[i] = r.nextFloat() * 2.0f - 1.0f;
        vx = FloatVector.fromArray(PFS, x, 0);

		y = r.nextFloat() * 2.0f - 1.0f;
		vy = FloatVector.zero(PFS).blend(y, MASK_C_EVEN);
	}

	@Benchmark
	public void addScalarWithMask(Blackhole bh) {
		bh.consume(vx.add(y, MASK_C_EVEN));
	}

	@Benchmark
	public void createAndAddMaskedVector(Blackhole bh) {
		bh.consume(vx.add(FloatVector.zero(PFS).blend(y, MASK_C_EVEN)));
	}

    @Benchmark
    public void addCachedMaskedVector(Blackhole bh) {
        bh.consume(vx.add(vy));
    }
}
