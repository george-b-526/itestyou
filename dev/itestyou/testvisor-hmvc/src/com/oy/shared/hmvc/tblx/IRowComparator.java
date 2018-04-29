/*
	Hierarchical Model View Controller (OY-HMVC)
	Copyright (C) 2005-2008 Pavel Simakov
	http://www.softwaresecretweapons.com

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
*/


package com.oy.shared.hmvc.tblx;

import java.util.Comparator;

public interface IRowComparator {
 
	public int compare (TableCell [] row1, TableCell [] row2);
	
	class MyStringSorter implements IRowComparator {
		
		private int colIdx;
		
		public MyStringSorter(int colIdx){
			this.colIdx = colIdx;
		}
		
		private String getValue(TableCell cell){
			if (cell.getValue() == null){
				return "";
			} else {
				return cell.getValue();
			}
		}
		
		public int compare (TableCell [] row1, TableCell [] row2){
			String o1 = getValue(row1[colIdx]);
			String o2 = getValue(row2[colIdx]);
			return o1.toUpperCase().compareTo(o2.toUpperCase());
		}
	}  
	
	class MyNumberSorter implements IRowComparator {
		
		private int colIdx;
		
		public MyNumberSorter(int colIdx){
			this.colIdx = colIdx;
		}
		
		private Double getValue(TableCell cell){
			try {
				return Double.valueOf(cell.getValue());
			} catch (NumberFormatException nfe){
				return new Double(0);
			}
		}
		 
		public int compare (TableCell [] row1, TableCell [] row2){
			Double o1 = getValue(row1[colIdx]);
			Double o2 = getValue(row2[colIdx]);
			return o1.compareTo(o2);
		}
	} 
	
	class RowComparator implements Comparator {
		private IRowComparator comp;
		private RowComparator next;
		private boolean asc;
		public RowComparator(int colIdx, Class colType, boolean asc, RowComparator next){
			this.next = next;
			this.asc = asc;
			
			boolean isString = String.class.equals(colType);  
			boolean isNumber = 
				Integer.class.equals(colType)
				||
				Double.class.equals(colType);
			
			if (isNumber){
				comp = new MyNumberSorter(colIdx);
			} else { 
				if (isString){
					comp = new MyStringSorter(colIdx);
				} else {
					throw new RuntimeException("Please use String, Integer or Doble as sorting types.");	
				}
			}
		}
		public int compare(Object obj1, Object obj2){
			TableCell [] t1 = (TableCell []) obj1;
			TableCell [] t2 = (TableCell []) obj2;

			int compare;
			if (asc){
				compare = comp.compare(t1, t2);
			} else  {
				compare = comp.compare(t2, t1);
			}
			
			if (compare == 0 && next != null){
				compare = next.compare(t1, t2);
			}
			
			return compare;
		} 
	} 
	
}
