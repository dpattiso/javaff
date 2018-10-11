package javaff.graph;

import java.util.Collection;

/**
 * Represents a mutex between a single object and 1-n others.
 * 
 * @author David Pattison
 *
 */
public interface Mutex<T>
{
	public void addMutex(T other);

	public boolean removeMutex(T other);

	public boolean hasMutexes();

	public boolean isMutexWith(T a);

	public T getOwner();

	public void setOwner(T owner);

	public Collection<T> getOthers();

	public void setOthers(Collection<T> others);

}