#THIS IS A TEMPLATE - CHANGE AT YOUR OWN RISK!

#establish error logging (Taken from: https://stackoverflow.com/questions/11666086/)
zz <- file("Errorlog.Rout", open="wt")
sink(zz, type="message")

#Don't manipulate this line - it is automatically replaced by the program!
filename <- REPLACEME

results <- read.table(filename, sep=",", row.names=1, header=TRUE)
results <- within(results, Sites.ratio <- Sites.successful/Sites.total)
results <- within(results, Space.ratio <- Space.used/Space.total)
results <- within(results, TimeToSites <- Time/Sites.total)
results <- within(results, SpaceToSites <- Space.used/Sites.total)

plot(results[,'Time'], ylab="Time (ns)", type="b", main="Plot of time elapsed")
boxplot(results$Time, ylab="Time (ns)", main="Time elapsed (Outliers removed)", outline=FALSE)
boxplot(results$TimeToSites, main="Average time per site (Outliers removed)", outline=FALSE)
boxplot(results$Sites.ratio, main="Percentage of successful routings")
boxplot(results$Space.ratio, main="Percentage of Label Area usage")
plot(results$Sites.total,results$Space.used, xlab="Sites in text", ylab="Label area space used", main="Label area usage by number of sites")
#with(results, abline(lm(Sites.total ~ Space.used)))



## reset message sink and close the file connection
sink(type="message")
close(zz)

#User feedback
message("Maximum Values for:")
colMax <- function(data) sapply(data, max, na.rm = TRUE)
colMax(results)
message("Calculations completed successfully.")
message("Please do not close the window prematurely.")#Closing the window early seems to corrupt the PDF with the plots
Sys.sleep(1)
