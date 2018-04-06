#establish error logging (Taken from: https://stackoverflow.com/questions/11666086/)
zz <- file("Errorlog.Rout", open="wt")
sink(zz, type="message")

filename <- "Test_2018-04-06_17-37-47.log"#Replace with name of file to evaluate.

results <- read.table(filename, sep=",", row.names=1, header=TRUE)
results <- within(results, Sites.ratio <- Sites.successful/Sites.total)
results <- within(results, Space.ratio <- Space.used/Space.total)
results <- within(results, TimeToSites <- Time/Sites.total)

boxplot(results$Time, main="Time elapsed (Outliers removed)", outline=FALSE)
boxplot(results$Time, main="Time elapsed", outline=TRUE)
boxplot(results$TimeToSites, main="Average time per site")
boxplot(results$Sites.ratio, main="Percentage of successful routings")
boxplot(results$Space.ratio, main="Percentage of Label Area usage")


## reset message sink and close the file connection
sink(type="message")
close(zz)

#User feedback
colMax <- function(data) sapply(data, max, na.rm = TRUE)
colMax(results)
message("Calculations completed successfully.")
message("Please do not close the window prematurely.")
Sys.sleep(10)
