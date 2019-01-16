![Example Waterfall](examples/image_1.png)

**Heatmap** is a piece of software for creating a heatmap from [rtl_power](https://github.com/keenerd/rtl-sdr) csv data collected using RTL-SDR software defined radio. Usefull for finding active frequencies in the rf spectrum. All decibel values are normalized and then drawn using Princeton's [StdDraw](https://introcs.cs.princeton.edu/java/stdlib/javadoc/StdDraw.html) library. Blue represents the highest and red the lowest value. At the moment very large files cannot yet be processed. 


### Usage:
To see some examples, view [example 1](examples/survey_1.png) and [example 2](examples/survey_2.png). 

1. Download repository

    `git clone https://github.com/gue-ni/heatmap.git` <br>
    `cd build` <br>

2. Collect signal data: 

    24h survey of the entire spectrum covered by the RTL-SDR:<br>
    `rtl_power -f 24M:1700M:1M -i 100 -g 50 -e 24h data.csv` 
  
    Survey of the upper 19 MHz Airband for one hour. This can be used to find active air traffic control channels. <br>
    `rtl_power -f 118M:140M:8k -i 10 -g 50 1h airband.csv` 
  
3. Process data and create heatmap: <br>

    `java -jar RadioHeatmap.jar -f file.csv -i filename -t [png/jpeg] [OPTIONS]` 
  
    Use this command to create and label the heatmap. <br>
    `java -jar RadioHeatmap.jar -f data.csv -i image -t png -l` 
    <br>

    ```
    Usage: java -jar RadioHeatmap.jar -f file -i image -t filetype [OPTIONS]
        -f      path to csv source file [-f example.csv]
        -i      name of target image file [-i example]
        -t      image file type [-t png/jpeg]
        -p      Print raw data
        -h      help
        -s      draw scale on heatmap
        -l      draw label on heatmap
        -deb    debugging mode

    Example: java -jar RadioHeatmap.jar -f survey.csv -i survey -t png`
    ```

