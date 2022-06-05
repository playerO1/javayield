# javayield
Python-style yield iterator for Java

Do you see python yield for return part of function work, like as generator? Now you can do this on Java with my little class.

## Example source:

        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                for (int i=0;i<10;i++) yield(i); // generate objects
            }
        };
        
        // Collect iterated data
        while (iterator.hasNext()) {
            Integer i=iterator.next();
            System.out.print(" "+i);
        }

See javadoc in classes.
'YieldIterator' using thread for support return part of work. You should be ensure close it.
