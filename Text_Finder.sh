echo "Searching for all instances of parse in the source tree"
echo
for I in `find . -type f -name "*.java" -print`
do
L=`grep -n "com.occamlab.te.spi" $I`
if [ "$L" ] 
then
echo $I
echo $L
echo
fi
done


 
