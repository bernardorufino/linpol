package br.com.bernardorufino.linpol.model;

import java.io.Serializable;
import java.util.ArrayList;

public class InterpolationSet {
	private ArrayList<float[]> set;
	private float coefficient;
	
	public InterpolationSet() {
		set = new ArrayList<float[]>();
	}
	
	public InterpolationSet(Serializable set) {
		@SuppressWarnings("unchecked")
		ArrayList<float[]> tmpSet = (ArrayList<float[]>) set;
		this.set = tmpSet;
	}
	
	public int addProperty() {
		set.add(new float[2]);
		return set.size() - 1;
	} 
	
	public void removeProperty(int i) {
		set.remove(i);
	}
	
	public void setLeftValue(int i, float x) {set.get(i)[0] = x;}	
	public void setRightValue(int i, float x) {set.get(i)[1] = x;}	
	public float getLeftValue(int i) {return set.get(i)[0];}	
	public float getRightValue(int i) {return set.get(i)[1];}
	public float[] getSideValues(int i) {return set.get(i);}
	public void setValue(int i, int n, float x) {
		set.get(i)[n] = x;
	}
	
	public int size() {
		return set.size();
	}
	
	public float getCoefficient() {
		return coefficient;
	}
	
	public void setControlValue(int i, float x) {
		coefficient = coefficient(getLeftValue(i), x, getRightValue(i));
	}
	
	public float getValue(int i) {
		return interpolate(coefficient, getLeftValue(i), getRightValue(i));
	}
	
	public ArrayList<float[]> toSerializable() {
		return set;
	}
	
	private float coefficient(float a, float x, float b) {
		return (x - a)/(b - a);
	}
	
	private float interpolate(float coeficient, float a, float b) {
		return a + coeficient * (b - a);
	}
}
