/*
 * Copyright 2013 Basho Technologies Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.basho.riak.client.core;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RiakFutures
{

	public static <T> RiakFuture<T> immediateFuture(T value)
	{
		return new ImmediateFuture<T>(value);
	}

	public static <T> RiakFuture<T> immediateFailedFuture(Exception e)
	{
		return new FailedFuture<T>(e);
	}

	private static class ImmediateFuture<T> implements RiakFuture<T>
	{

		private final T value;

		private ImmediateFuture(T value)
		{
			this.value = value;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning)
		{
			return false;
		}

		@Override
		public T get() throws InterruptedException, ExecutionException
		{
			return value;
		}

		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
		{
			return value;
		}

		@Override
		public boolean isCancelled()
		{
			return false;
		}

		@Override
		public boolean isDone()
		{
			return true;
		}

		@Override
		public void addListener(RiakFutureListener<T> listener)
		{
			listener.handle(this);
		}

		@Override
		public void removeListener(RiakFutureListener<T> listener)
		{

		}
	}

	private static class FailedFuture<T> implements RiakFuture<T>
	{

		private final Exception e;

		private FailedFuture(Exception e)
		{
			this.e = e;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning)
		{
			return false;
		}

		@Override
		public T get() throws InterruptedException, ExecutionException
		{
			throw new ExecutionException(e);
		}

		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
		{
			throw new ExecutionException(e);
		}

		@Override
		public boolean isCancelled()
		{
			return false;
		}

		@Override
		public boolean isDone()
		{
			return true;
		}

		@Override
		public void addListener(RiakFutureListener<T> listener)
		{
			listener.handle(this);
		}

		@Override
		public void removeListener(RiakFutureListener<T> listener)
		{

		}
	}

}
